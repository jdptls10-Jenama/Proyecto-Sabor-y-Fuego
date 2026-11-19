package com.restaurante.service;

import com.restaurante.model.Categoria;
import com.restaurante.model.Restaurante;
import com.restaurante.repository.CategoriaRepository;
import com.restaurante.repository.RestauranteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final RestauranteRepository restauranteRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, RestauranteRepository restauranteRepository) {
        this.categoriaRepository = categoriaRepository;
        this.restauranteRepository = restauranteRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar(Long restauranteId) {
        return categoriaRepository.findAllByRestaurante_IdOrderByNombreAsc(restauranteId);
    }

    @Transactional
    public Categoria obtenerOCrear(String nombre, Long restauranteId) {
        if (nombre == null || nombre.isBlank()) return null;
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado."));
        return categoriaRepository.findByNombreIgnoreCaseAndRestaurante_Id(nombre.trim(), restauranteId)
                .orElseGet(() -> {
                    Categoria categoria = new Categoria(nombre.trim(), "");
                    categoria.setRestaurante(restaurante);
                    return categoriaRepository.save(categoria);
                });
    }

    @Transactional
    public void eliminar(Long categoriaId, Long restauranteId) {
        Categoria categoria = categoriaRepository.findByIdAndRestaurante_Id(categoriaId, restauranteId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada en este restaurante."));
        categoriaRepository.delete(categoria);
    }
}