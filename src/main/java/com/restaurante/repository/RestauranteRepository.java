package com.restaurante.repository;

import com.restaurante.model.Restaurante;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
    @EntityGraph(attributePaths = "administrador")
    Optional<Restaurante> findBySlugAndActivoTrue(String slug);
    @EntityGraph(attributePaths = "administrador")
    Optional<Restaurante> findBySlug(String slug);
    @EntityGraph(attributePaths = "administrador")
    Optional<Restaurante> findFirstByAdministrador_IdOrderByNombreAsc(Long administradorId);
    boolean existsBySlug(String slug);
    @EntityGraph(attributePaths = "administrador")
    List<Restaurante> findAllByOrderByNombreAsc();
}