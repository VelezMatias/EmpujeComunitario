package com.empuje.ui;

import com.empuje.ui.api.dto.*;
import com.empuje.ui.service.FiltroEventosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/informes/eventos/filtros")
@Tag(name = "Filtros guardados del Informe de Participación")
public class FiltroEventosController {

  private final FiltroEventosService service;

  public FiltroEventosController(FiltroEventosService service) { this.service = service; }

  @GetMapping
  @Operation(summary = "Listar mis filtros guardados")
  public List<FiltroEventosRes> listar() {
    return service.listarMios();
  }

  @PostMapping
  @Operation(summary = "Crear filtro nuevo")
  public ResponseEntity<FiltroEventosRes> crear(@RequestBody FiltroEventosCreateReq req) {
    return ResponseEntity.ok(service.crear(req));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Actualizar un filtro existente")
  public ResponseEntity<FiltroEventosRes> actualizar(@PathVariable Long id,
                                                     @RequestBody FiltroEventosUpdateReq req) {
    return ResponseEntity.ok(service.actualizar(id, req));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Borrar un filtro")
  public ResponseEntity<Void> borrar(@PathVariable Long id) {
    service.borrar(id);
    return ResponseEntity.noContent().build();
  }
}
