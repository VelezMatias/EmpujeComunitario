package com.empuje.ui.solicitudes;

import com.empuje.kafka.entity.SolicitudItem;
import com.empuje.kafka.repo.SolicitudExternaRepo;
import com.empuje.kafka.repo.SolicitudItemRepo;
import com.empuje.grpc.ong.DonationServiceGrpc;
import com.empuje.grpc.ong.TransferDonationItem;
import com.empuje.grpc.ong.TransferDonationsRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;

@Controller
@RequestMapping("/ui/solicitudes")
public class SolicitudesTransferController {

    private final DonationServiceGrpc.DonationServiceBlockingStub donationStub;
    private final SolicitudItemRepo itemsRepo;
    private final SolicitudExternaRepo externasRepo;

    public SolicitudesTransferController(
            DonationServiceGrpc.DonationServiceBlockingStub donationStub,
            SolicitudItemRepo itemsRepo,
            SolicitudExternaRepo externasRepo
    ) {
        this.donationStub = donationStub;
        this.itemsRepo = itemsRepo;
        this.externasRepo = externasRepo;
    }

    @PostMapping("/{solicitudId}/transferir")
    public String transferir(@PathVariable String solicitudId,
                             @RequestParam(name = "orgId", required = false) Integer orgReceptoraId,
                             RedirectAttributes ra) {

        var items = itemsRepo.findBySolicitudId(solicitudId).stream()
                .sorted(Comparator.comparing(SolicitudItem::getId))
                .toList();

        var reqBuilder = TransferDonationsRequest.newBuilder()
                .setSolicitudId(solicitudId)
                .setOrgReceptoraId(orgReceptoraId == null ? 0 : orgReceptoraId);

        for (var it : items) {
            var ti = TransferDonationItem.newBuilder()
                    .setCategoria(it.getCategoria() == null ? "" : it.getCategoria())
                    .setDescripcion(it.getDescripcion() == null ? "" : it.getDescripcion())
                    .setCantidad(it.getCantidad() == null ? 0.0 : it.getCantidad().doubleValue())
                    .build();
            reqBuilder.addItems(ti);
        }

        try {
            var resp = donationStub.transferDonations(reqBuilder.build());

            // ✅ Solo si el server confirmó, marcamos CUMPLIDA
            externasRepo.findBySolicitudId(solicitudId).ifPresent(ext -> {
                ext.setEstado("CUMPLIDA");
                externasRepo.save(ext);
            });

            ra.addFlashAttribute("msg", "Transferencia publicada: " + resp.getTransferUid());
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().getCode() == Status.Code.FAILED_PRECONDITION) {
                // Mensaje de validación de stock enviado por el servidor
                String det = ex.getStatus().getDescription();
                if (det == null || det.isBlank()) det = "No hay inventario disponible o stock insuficiente.";
                ra.addFlashAttribute("error", det);
            } else {
                ra.addFlashAttribute("error", "No se pudo transferir: " + ex.getStatus().getDescription());
            }
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Error inesperado: " + ex.getMessage());
        }
        return "redirect:/ui/solicitudes";
    }
}
