package com.empuje.kafka.consumer;

import com.empuje.kafka.config.OrgConfig;
import com.empuje.kafka.entity.SolicitudCumplida;
import com.empuje.kafka.repo.SolicitudCumplidaRepo;
import com.empuje.kafka.repo.SolicitudExternaRepo;
import com.empuje.kafka.repo.SolicitudRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

// ==== gRPC (generado desde tu Proto ong.proto) ====
import com.empuje.grpc.ong.ApiResponse;
import com.empuje.grpc.ong.AuthContext;
import com.empuje.grpc.ong.CreateDonationRequest;
import com.empuje.grpc.ong.DonationServiceGrpc;
import com.empuje.grpc.ong.Role;
import com.empuje.grpc.ong.Category;

@Component
public class TransferenciasConsumer {

  private static final Logger log = LoggerFactory.getLogger(TransferenciasConsumer.class);

  private final ObjectMapper om = new ObjectMapper();
  private final OrgConfig org;
  private final DonationServiceGrpc.DonationServiceBlockingStub donationStub;
  private final SolicitudExternaRepo solicitudesExternas;
  private final SolicitudRepository solicitudesPropias;
  private final SolicitudCumplidaRepo cumplidas;

  public TransferenciasConsumer(
      OrgConfig org,
      DonationServiceGrpc.DonationServiceBlockingStub donationStub,
      SolicitudExternaRepo solicitudesExternas,
      SolicitudRepository solicitudesPropias,
      SolicitudCumplidaRepo cumplidas
  ) {
    this.org = org;
    this.donationStub = donationStub;
    this.solicitudesExternas = solicitudesExternas;
    this.solicitudesPropias = solicitudesPropias;
    this.cumplidas = cumplidas;
  }

  @KafkaListener(topics = "transferencia-donaciones", groupId = "${ORG_ID:42}-transfer")
  @Transactional
  public void onTransfer(ConsumerRecord<String, String> rec) {
    // Solo proceso si la key (org_receptora_id) coincide con mi ORG_ID
    if (!String.valueOf(org.getOrgId()).equals(rec.key())) return;

    log.info("[TRANSFER] RX topic={} key={} offset={} value={}",
        rec.topic(), rec.key(), rec.offset(), rec.value());

    try {
      Map<?, ?> m = om.readValue(rec.value(), Map.class);

      final String solicitudId = str(m.get("solicitud_id"));  // ej. "SOL-2025-XXXX"
      if (solicitudId.isBlank()) {
        log.warn("[TRANSFER] payload sin 'solicitud_id'. Ignoro.");
        return;
      }

      Object itemsRaw = m.get("items");
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> items = (itemsRaw instanceof List<?> l)
          ? (List<Map<String, Object>>) (List<?>) l
          : List.of();

      if (items.isEmpty()) {
        log.warn("[TRANSFER] payload sin 'items' solicitud_id={}", solicitudId);
        return;
      }

      // ---- Auth gRPC: usar un rol permitido por el server Python (PRESIDENTE o VOCAL) ----
     AuthContext auth = AuthContext.newBuilder()
      .setActorId(1)            // <-- ID de un usuario que exista en tu tabla usuarios
      .setActorRole(Role.VOCAL) // (o PRESIDENTE)
      .build();

      int ok = 0;
      for (var it : items) {
        String categoriaTxt = str(it.get("categoria")); // texto en Kafka
        String descripcion  = str(it.get("descripcion"));
        Double cantidadDbl  = dbl(it.get("cantidad"));
        if (cantidadDbl == null) cantidadDbl = 0.0;
        int cantidad = (int) Math.round(cantidadDbl);   // el proto pide int32

        // Mapear texto -> enum Category del proto
        Category categoriaEnum = toCategoryEnum(categoriaTxt);

        CreateDonationRequest req = CreateDonationRequest.newBuilder()
            .setAuth(auth)
            .setCategoria(categoriaEnum)          // enum
            .setDescripcion(descripcion)
            .setCantidad(cantidad)                // int32
            .build();

        ApiResponse resp = donationStub.createDonationItem(req);

        if (resp.getSuccess()) {
          ok++;
          log.info("[TRANSFER→gRPC] OK: {}", resp.getMessage());
        } else {
          log.warn("[TRANSFER→gRPC] FALLÓ: msg='{}' cat='{}' desc='{}' cant={}",
              resp.getMessage(), categoriaTxt, descripcion, cantidad);
        }
      }

      // === Marcado de solicitud como CUMPLIDA (igual que venías haciendo) ===
      boolean marcada = false;

      // 1) Si es una solicitud EXTERNA local, marcar estado
      var ext = solicitudesExternas.findBySolicitudId(solicitudId);
      if (ext.isPresent()) {
        ext.get().setEstado("CUMPLIDA");
        solicitudesExternas.save(ext.get());
        log.info("[TRANSFER] Marcada EXTERNA como CUMPLIDA: {}", solicitudId);
        marcada = true;
      }

      // 2) Si es PROPIA, registrar en solicitudes_cumplidas (idempotente)
      if (!marcada && solicitudesPropias.findBySolicitudId(solicitudId).isPresent()) {
        if (!cumplidas.existsBySolicitudId(solicitudId)) {
          cumplidas.save(new SolicitudCumplida(solicitudId));
          log.info("[TRANSFER] Marcada PROPIA como CUMPLIDA: {}", solicitudId);
        } else {
          log.info("[TRANSFER] PROPIA ya estaba CUMPLIDA: {}", solicitudId);
        }
      }

      if (ok == 0) {
        log.warn("[TRANSFER] Atención: gRPC no registró ninguna donación para solicitud={}. "
            + "Revisá nombre de categorías válidas y el rol/actorId.", solicitudId);
      }

      log.info("[TRANSFER] Fin solicitud={} items_ok={} items_total={}", solicitudId, ok, items.size());

    } catch (Exception e) {
      log.error("[TRANSFER][ERR] {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  // ===== Helpers =====
  private static String str(Object o) { return o == null ? "" : String.valueOf(o).trim(); }
  private static Double dbl(Object o) {
    try { return o == null ? null : Double.parseDouble(o.toString()); } catch (Exception e) { return null; }
  }

  // Mapeo robusto de nombre de categoría -> enum Category
  private static Category toCategoryEnum(String s) {
    if (s == null) return Category.CATEGORY_UNSPECIFIED;
    String x = s.trim().toUpperCase()
        .replace('Á','A').replace('É','E').replace('Í','I').replace('Ó','O').replace('Ú','U')
        .replace('Ü','U')
        .replace(' ', '_').replace('-', '_');

    return switch (x) {
      case "ALIMENTOS", "COMIDA" -> Category.ALIMENTOS;
      case "ROPA" -> Category.ROPA;
      case "JUGUETES", "JUGUETE" -> Category.JUGUETES;
      case "UTILES_ESCOLARES", "UTILES" -> Category.UTILES_ESCOLARES;
      default -> Category.CATEGORY_UNSPECIFIED;
    };
  }
}
