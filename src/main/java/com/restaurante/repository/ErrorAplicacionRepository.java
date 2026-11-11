
package com.restaurante.repository;

import com.restaurante.model.ErrorAplicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ErrorAplicacionRepository
        extends JpaRepository<ErrorAplicacion, Long> {

    List<ErrorAplicacion> findTop100ByOrderByFechaErrorDesc();

    List<ErrorAplicacion> findByEstadoOrderByFechaErrorDesc(
            String estado
    );

    long countByEstado(String estado);
}
