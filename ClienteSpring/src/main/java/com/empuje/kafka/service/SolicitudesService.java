package com.empuje.kafka.service;

import com.empuje.kafka.repo.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitudesService {

  private final SolicitudRepository repo;

  public SolicitudesService(SolicitudRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public int eliminarPropia(String solicitudId, int orgId) {
    // seguridad extra: no permitir borrar si ya está cumplida
    if (repo.isCumplida(solicitudId)) {
      throw new IllegalStateException("La solicitud " + solicitudId + " ya está CUMPLIDA y no puede eliminarse.");
    }
    // borrar marca cumplida por si quedó colgada (no debería si llega acá)
    repo.deleteCumplida(solicitudId);

    // borra la solicitud (solo si pertenece a tu organización)
    return repo.deleteBySolicitudIdAndOrgId(solicitudId, orgId);
  }
}
