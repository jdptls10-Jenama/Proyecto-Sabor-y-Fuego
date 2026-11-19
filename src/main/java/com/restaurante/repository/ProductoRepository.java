package com.restaurante.repository;

import com.restaurante.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findAllByRestaurante_IdOrderByNombreAsc(Long restauranteId);

    Optional<Producto> findByIdAndRestaurante_Id(Long productoId, Long restauranteId);

    boolean existsByIdAndRestaurante_Id(Long productoId, Long restauranteId);
}