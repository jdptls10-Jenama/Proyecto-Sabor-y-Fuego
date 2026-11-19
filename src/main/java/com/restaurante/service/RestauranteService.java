package com.restaurante.service;

import com.restaurante.model.Restaurante;
import com.restaurante.repository.RestauranteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class RestauranteService {

    private final RestauranteRepository repository;

    public RestauranteService(RestauranteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Restaurante crear(String nombre, String slugSolicitado) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del restaurante es obligatorio.");
        }

        String slug = normalizarSlug(
                slugSolicitado == null || slugSolicitado.isBlank()
                        ? nombre : slugSolicitado
        );

        if (slug.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un identificador válido para el enlace.");
        }

        if (repository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Ese identificador de enlace ya está ocupado.");
        }

        Restaurante restaurante = new Restaurante(nombre.trim(), slug);
        restaurante.setTituloInicio("Bienvenido a " + nombre.trim());
        restaurante.setDescripcionInicio("Conoce nuestro menú y realiza tu pedido.");
        return repository.save(restaurante);
    }

    @Transactional(readOnly = true)
    public Optional<Restaurante> buscarPorSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        return repository.findBySlugAndActivoTrue(normalizarSlug(slug));
    }

    @Transactional(readOnly = true)
    public List<Restaurante> listar() {
        return repository.findAllByOrderByNombreAsc();
    }

    private String normalizarSlug(String valor) {
        String sinAcentos = Normalizer.normalize(
                valor.trim().toLowerCase(Locale.ROOT),
                Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");

        return sinAcentos.replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
