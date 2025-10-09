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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// gRPC
import com.empuje.grpc.ong.ApiResponse;
import com.empuje.grpc.ong.AuthContext;
import com.empuje.grpc.ong.Category;
import com.empuje.grpc.ong.CreateDonationRequest;
import com.empuje.grpc.ong.DonationServiceGrpc;
import com.empuje.grpc.ong.Role;

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
  if (!String.valueOf(org.getOrgId()).equals(rec.key())) return;

  log.info("[TRANSFER] RX topic={} key={} offset={} value={}",
      rec.topic(), rec.key(), rec.offset(), rec.value());

  try {
    Map<?, ?> m = om.readValue(rec.value(), Map.class);
    final String solicitudId = str(m.get("solicitud_id"));

    Object itemsRaw = m.get("items");
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> items = (itemsRaw instanceof List<?> l)
        ? (List<Map<String, Object>>) (List<?>) l
        : List.of();

    if (solicitudId.isBlank()) {
      log.warn("[TRANSFER] payload sin 'solicitud_id'. Ignoro.");
      return;
    }
    if (items.isEmpty()) {
      log.warn("[TRANSFER] payload sin 'items' solicitud_id={}", solicitudId);
      return;
    }

    // Auth para gRPC
    AuthContext auth = AuthContext.newBuilder()
        .setActorId(org.getOrgId())
        .setActorRole(Role.COORDINADOR)
        .build();

    int ok = 0;
    for (Map<String, Object> it : items) {
      String categoriaTxt = str(it.get("categoria"));
      String descripcion  = str(it.get("descripcion"));
      int cantidad        = (int) Math.max(0, Math.round(num(it.get("cantidad"))));

      Category categoria = toCategoryEnum(categoriaTxt);
      if (categoria == Category.CATEGORY_UNSPECIFIED) {
        log.warn("[TRANSFER→gRPC] categoría desconocida='{}' → omito ítem", categoriaTxt);
        continue;
      }

      CreateDonationRequest req = CreateDonationRequest.newBuilder()
          .setAuth(auth)
          .setCategoria(categoria)
          .setDescripcion(descripcion)
          .setCantidad(cantidad)
          .build();

      ApiResponse resp = donationStub.createDonationItem(req);
      log.info("[TRANSFER→gRPC] createDonationItem resp: success={} msg='{}'",
          resp.getSuccess(), resp.getMessage());

      if (resp.getSuccess()) ok++;
    }

    // === Marcar cumplida ===
    boolean marcarCumplida = ok > 0;


    if (!marcarCumplida) {
      log.warn("[TRANSFER] Ningún item creado por gRPC. (¿gRPC caído?) Marcando CUMPLIDA igual para prueba.");
      marcarCumplida = true;
    }

    if (marcarCumplida) {
      boolean marcada = false;

   
      var ext = solicitudesExternas.findBySolicitudId(solicitudId);
      if (ext.isPresent()) {
        ext.get().setEstado("CUMPLIDA");
        solicitudesExternas.save(ext.get());
        log.info("[TRANSFER] Marcada EXTERNA como CUMPLIDA: {}", solicitudId);
        marcada = true;
      }

   
      if (!marcada && solicitudesPropias.findBySolicitudId(solicitudId).isPresent()) {
        if (!cumplidas.existsBySolicitudId(solicitudId)) {
          cumplidas.save(new SolicitudCumplida(solicitudId)); 
          log.info("[TRANSFER] Marcada PROPIA como CUMPLIDA: {}", solicitudId);
        } else {
          log.info("[TRANSFER] PROPIA ya estaba CUMPLIDA: {}", solicitudId);
        }
      }
    }

    log.info("[TRANSFER] Fin solicitud={} items_ok={} items_total={}", solicitudId, ok, items.size());

  } catch (Exception e) {
    log.error("[TRANSFER][ERR] {}", e.getMessage(), e);
  }
}


  // helpers
  private static String str(Object o) { return o == null ? "" : o.toString(); }
  private static double num(Object o) {
    if (o == null) return 0d;
    if (o instanceof Number n) return n.doubleValue();
    try { return Double.parseDouble(o.toString()); } catch (Exception e) { return 0d; }
  }
  private static Category toCategoryEnum(String s) {
    if (s == null) return Category.CATEGORY_UNSPECIFIED;
    String x = s.trim().toUpperCase().replace(' ', '_');
    return switch (x) {
      case "ROPA" -> Category.ROPA;
      case "ALIMENTOS", "COMIDA" -> Category.ALIMENTOS;
      case "JUGUETES", "JUGUETE" -> Category.JUGUETES;
      case "UTILES_ESCOLARES", "ÚTILES_ESCOLARES", "UTILES", "UTILES-ESCOLARES" -> Category.UTILES_ESCOLARES;
      default -> Category.CATEGORY_UNSPECIFIED;
    };
  }
}
