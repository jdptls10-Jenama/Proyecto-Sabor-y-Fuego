package com.restaurante.repository;

import com.restaurante.model.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {

    Optional<Restaurante> findBySlugAndActivoTrue(String slug);

    Optional<Restaurante> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Restaurante> findAllByOrderByNombreAsc();
}
