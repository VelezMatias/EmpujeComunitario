package com.empuje.graphql;

import com.empuje.ui.service.FiltroDonacionesService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class DonationFilterResolver {

    private final FiltroDonacionesService service;

    public DonationFilterResolver(FiltroDonacionesService service) {
        this.service = service;
    }

    /* ========================= Query ========================= */

    
    // Devuelve los filtros de donaciones guardados del usuario logueado.
    // La UI (donaciones.html) usa esta query para poblar el <select>.
     
    @QueryMapping
    public List<DonationFilter> myFilters() {
        return service.listMine();
    }

    /* ======================== Mutations ======================= */

    
    // Crea un filtro persistente de donaciones.
    
    @MutationMapping
    public DonationFilter saveFilter(@Argument("input") FilterInput input) {
        return service.create(
                input.getNombre(),
                input.getCategoria(),
                input.getFrom(),
                input.getTo(),
                input.getEliminado());
    }

   
     // Actualiza un filtro persistente de donaciones (debe pertenecer al usuario actual)
     
    @MutationMapping
    public DonationFilter updateFilter(@Argument("id") Long id,
            @Argument("input") FilterInput input) {
        return service.update(
                id,
                input.getNombre(),
                input.getCategoria(),
                input.getFrom(),
                input.getTo(),
                input.getEliminado());
    }

     // Elimina un filtro persistente de donaciones.

    @MutationMapping
    public Boolean deleteFilter(@Argument("id") Long id) {
        return service.delete(id);
    }
}
