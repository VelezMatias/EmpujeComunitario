package com.empuje.ui.service;

import com.empuje.ui.api.dto.*;
import com.empuje.ui.entity.FiltroEventos;
import com.empuje.ui.repo.FiltroEventosRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FiltroEventosService {

  private final FiltroEventosRepo repo;
  private final HttpSession session;

  public FiltroEventosService(FiltroEventosRepo repo, HttpSession session) {
    this.repo = repo;
    this.session = session;
  }

  private Long currentUserId() {
    Object v = session != null ? session.getAttribute("userId") : null;
    if (v instanceof Number n) return n.longValue();
    if (v instanceof String s) try { return Long.parseLong(s); } catch (NumberFormatException ignore) {}
    throw new IllegalStateException("Sesión inválida o no autenticada");
  }

  private boolean esPresidenteOCoor() {
    Object r = session != null ? session.getAttribute("rol") : null;
    String rol = (r == null) ? "" : r.toString();
    return "PRESIDENTE".equalsIgnoreCase(rol) || "COORDINADOR".equalsIgnoreCase(rol);
  }

  public List<FiltroEventosRes> listarMios() {
    Long owner = currentUserId();
    return repo.findByOwnerUserIdOrderByNombreAsc(owner)
        .stream().map(this::toRes).toList();
  }

  @Transactional
  public FiltroEventosRes crear(FiltroEventosCreateReq req) {
    Long owner = currentUserId();
    validarUsuarioObjetivo(req.usuarioObjetivoId(), owner);
    FiltroEventos e = new FiltroEventos();
    e.setOwnerUserId(owner);
    e.setNombre(req.nombre().trim());
    e.setUsuarioObjetivoId(req.usuarioObjetivoId());
    e.setFechaDesde(req.fechaDesde());
    e.setFechaHasta(req.fechaHasta());
    e.setReparto(req.reparto());
    return toRes(repo.save(e));
  }

  @Transactional
  public FiltroEventosRes actualizar(Long id, FiltroEventosUpdateReq req) {
    Long owner = currentUserId();
    FiltroEventos e = repo.findByIdAndOwnerUserId(id, owner)
        .orElseThrow(() -> new IllegalArgumentException("Filtro no encontrado"));
    validarUsuarioObjetivo(req.usuarioObjetivoId(), owner);
    e.setNombre(req.nombre().trim());
    e.setUsuarioObjetivoId(req.usuarioObjetivoId());
    e.setFechaDesde(req.fechaDesde());
    e.setFechaHasta(req.fechaHasta());
    e.setReparto(req.reparto());
    return toRes(repo.save(e));
  }

  @Transactional
  public void borrar(Long id) {
    Long owner = currentUserId();
    FiltroEventos e = repo.findByIdAndOwnerUserId(id, owner)
        .orElseThrow(() -> new IllegalArgumentException("Filtro no encontrado"));
    repo.delete(e);
  }

  private void validarUsuarioObjetivo(Long usuarioObjetivoId, Long owner) {
    if (usuarioObjetivoId == null) throw new IllegalArgumentException("usuarioObjetivoId es requerido");
    if (!esPresidenteOCoor() && !owner.equals(usuarioObjetivoId)) {
      throw new IllegalArgumentException("Solo puede guardar filtros para su propio usuario");
    }
  }

  private FiltroEventosRes toRes(FiltroEventos f) {
    return new FiltroEventosRes(
        f.getId(), f.getNombre(), f.getUsuarioObjetivoId(),
        f.getFechaDesde(), f.getFechaHasta(), f.getReparto());
  }
}
