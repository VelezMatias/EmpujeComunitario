package com.empuje.ui.solicitudes;

import com.empuje.kafka.repo.SolicitudExternaRepo;
import com.empuje.kafka.repo.SolicitudItemRepo;     // <-- tu repo existente
import com.empuje.kafka.entity.SolicitudItem;
import com.empuje.grpc.ong.DonationServiceGrpc;
import com.empuje.grpc.ong.TransferDonationItem;
import com.empuje.grpc.ong.TransferDonationsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;

@Controller
@RequestMapping("/ui/solicitudes")
public class SolicitudesTransferController {

    private final SolicitudExternaRepo solicitudes;
    private final SolicitudItemRepo solicitudItemRepo;   
    private final DonationServiceGrpc.DonationServiceBlockingStub donationStub;

    @Autowired
    public SolicitudesTransferController(SolicitudExternaRepo solicitudes,
                                         SolicitudItemRepo solicitudItemRepo,
                                         DonationServiceGrpc.DonationServiceBlockingStub donationStub) {
        this.solicitudes = solicitudes;
        this.solicitudItemRepo = solicitudItemRepo;
        this.donationStub = donationStub;
    }

    @PostMapping("/{sid}/transferir")
    public String transferir(@PathVariable String sid, RedirectAttributes ra) {
        var se = solicitudes.findBySolicitudId(sid)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no existe: " + sid));

        if (se.getOrgId() == null) {
            ra.addFlashAttribute("error", "La solicitud no tiene org_id; no se puede transferir.");
            return "redirect:/ui/solicitudes";
        }

        // Traer ítems reales (y ordenar en memoria si querés)
        var itemsDb = solicitudItemRepo.findBySolicitudId(sid);
        itemsDb.sort(Comparator.comparing(SolicitudItem::getId));  // opcional

        if (itemsDb.isEmpty()) {
            ra.addFlashAttribute("error", "La solicitud no tiene ítems.");
            return "redirect:/ui/solicitudes";
        }

        var reqBuilder = TransferDonationsRequest.newBuilder()
                .setSolicitudId(sid)
                .setOrgReceptoraId(se.getOrgId());

        for (var it : itemsDb) {
            var ti = TransferDonationItem.newBuilder()
                    .setCategoria(it.getCategoria() == null ? "" : it.getCategoria())
                    .setDescripcion(it.getDescripcion() == null ? "" : it.getDescripcion())
                    .setCantidad(it.getCantidad() == null ? 0 : it.getCantidad().doubleValue())
                    .setUnidad(it.getUnidad() == null ? "" : it.getUnidad())
                    .build();
            reqBuilder.addItems(ti);
        }

        try {
            var resp = donationStub.transferDonations(reqBuilder.build());
            ra.addFlashAttribute("msg", "Transferencia publicada: " + resp.getTransferUid());
        } catch (io.grpc.StatusRuntimeException ex) {
            ra.addFlashAttribute("error", "No se pudo transferir: " + ex.getStatus().getDescription());
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Error inesperado: " + ex.getMessage());
        }
        return "redirect:/ui/solicitudes";
    }
}
