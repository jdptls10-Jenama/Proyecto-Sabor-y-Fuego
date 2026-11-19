package com.restaurante.repository;

import com.restaurante.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByRestaurante_IdOrderByNombreAsc(Long restauranteId);
    Optional<Categoria> findByNombreIgnoreCaseAndRestaurante_Id(String nombre, Long restauranteId);
    Optional<Categoria> findByIdAndRestaurante_Id(Long id, Long restauranteId);
}