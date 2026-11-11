
package com.restaurante.repository;

import com.restaurante.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActividadRepository
        extends JpaRepository<Actividad, Long> {

    List<Actividad> findTop100ByOrderByFechaEventoDesc();

    List<Actividad> findByTipoEventoOrderByFechaEventoDesc(
            String tipoEvento
    );

    long countByTipoEvento(String tipoEvento);
}
