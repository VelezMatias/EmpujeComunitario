package com.empuje.kafka.repo;

import com.empuje.kafka.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    /** Buscar por código lógico (ej. SOL-2025-XXXXXXX). */
    Optional<Solicitud> findBySolicitudId(String solicitudId);

    /** Todas las solicitudes propias (por org) ordenadas por fecha desc. */
    List<Solicitud> findAllByOrgIdOrderByFechaHoraDesc(Integer orgId);

    /* ==================== SOPORTE PARA ELIMINAR ==================== */

    /** ¿Está marcada como cumplida? (tabla solicitudes_cumplidas) */
    @Query(
        value = "select case when exists (select 1 from solicitudes_cumplidas where solicitud_id = :sid) then 1 else 0 end",
        nativeQuery = true
    )
    int _isCumplida(@Param("sid") String solicitudId);

    /** Helper Java: devuelve boolean en base al query anterior. */
    default boolean isCumplida(String solicitudId) {
        return _isCumplida(solicitudId) == 1;
    }

    /** Borra la marca de cumplida, si existe. */
    @Modifying
    @Transactional
    @Query(value = "delete from solicitudes_cumplidas where solicitud_id = :sid", nativeQuery = true)
    int deleteCumplida(@Param("sid") String solicitudId);

    /** Borra la solicitud propia validando org_id (dispara ON DELETE CASCADE en solicitud_prop_items). */
    @Modifying
    @Transactional
    @Query("delete from Solicitud s where s.solicitudId = :sid and s.orgId = :orgId")
    int deleteBySolicitudIdAndOrgId(@Param("sid") String solicitudId, @Param("orgId") Integer orgId);
}
