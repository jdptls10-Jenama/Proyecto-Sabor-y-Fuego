package com.restaurante.service;

import com.restaurante.model.Producto;
import com.restaurante.model.Restaurante;
import com.restaurante.repository.ProductoRepository;
import com.restaurante.repository.RestauranteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository repository;
    private final RestauranteRepository restauranteRepository;

    public ProductoService(ProductoRepository repository, RestauranteRepository restauranteRepository) {
        this.repository = repository;
        this.restauranteRepository = restauranteRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return listar(obtenerRestaurantePredeterminado().getId());
    }

    @Transactional(readOnly = true)
    public List<Producto> listar(Long restauranteId) {
        if (restauranteId == null) throw new IllegalArgumentException("Restaurante no válido.");
        return repository.findAllByRestaurante_IdOrderByNombreAsc(restauranteId);
    }

    @Transactional
    public Producto guardar(Producto producto) {
        return guardar(producto, obtenerRestaurantePredeterminado().getId());
    }

    @Transactional
    public Producto guardar(Producto producto, Long restauranteId) {
        if (producto == null || restauranteId == null) throw new IllegalArgumentException("Datos del producto no válidos.");
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado."));
        producto.setRestaurante(restaurante);
        return repository.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        eliminar(id, obtenerRestaurantePredeterminado().getId());
    }

    @Transactional
    public void eliminar(Long id, Long restauranteId) {
        Producto producto = repository.findByIdAndRestaurante_Id(id, restauranteId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en este restaurante."));
        repository.delete(producto);
    }

    private Restaurante obtenerRestaurantePredeterminado() {
        return restauranteRepository.findBySlug("sabor-y-fuego")
                .orElseThrow(() -> new IllegalStateException("No se ha ejecutado la migración inicial de restaurantes."));
    }
}