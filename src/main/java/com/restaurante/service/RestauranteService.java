package com.restaurante.service;

import com.restaurante.model.Restaurante;
import com.restaurante.model.Usuario;
import com.restaurante.repository.RestauranteRepository;
import com.restaurante.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.Normalizer;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class RestauranteService {
    private final RestauranteRepository repository;
    private final UsuarioRepository usuarioRepository;

    public RestauranteService(RestauranteRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Restaurante crear(String nombre, String slugSolicitado) {
        return crear(nombre, slugSolicitado, null);
    }

    @Transactional
    public Restaurante crear(String nombre, String slugSolicitado, Usuario creador) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre del restaurante es obligatorio.");
        String slug = normalizarSlug(slugSolicitado == null || slugSolicitado.isBlank() ? nombre : slugSolicitado);
        if (slug.isBlank()) throw new IllegalArgumentException("Debes indicar un identificador válido para el enlace.");
        if (repository.existsBySlug(slug)) throw new IllegalArgumentException("Ese identificador de enlace ya está ocupado.");
        Restaurante restaurante = new Restaurante(nombre.trim(), slug);
        restaurante.setAdministrador(creador);
        restaurante.setTituloInicio("Bienvenido a " + nombre.trim());
        restaurante.setDescripcionInicio("Conoce nuestro menú y realiza tu pedido.");
        return repository.save(restaurante);
    }

    @Transactional(readOnly = true)
    public Optional<Restaurante> buscarPorSlug(String slug) {
        if (slug == null || slug.isBlank()) return Optional.empty();
        return repository.findBySlugAndActivoTrue(normalizarSlug(slug));
    }

    @Transactional(readOnly = true)
    public List<Restaurante> listar() {
        return repository.findAllByOrderByNombreAsc();
    }

    @Transactional
    public Restaurante actualizarPersonalizacion(String slug, Usuario actor, String nombre,
            String logoUrl, String colorPrimario, String colorSecundario, String colorFondo,
            String imagenInicioUrl, String tituloInicio, String descripcionInicio) {
        Restaurante restaurante = buscarPorSlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado."));
        validarPermiso(restaurante, actor);
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio.");
        restaurante.setNombre(nombre.trim());
        restaurante.setLogoUrl(validarUrlOpcional(logoUrl, "logo"));
        restaurante.setImagenInicioUrl(validarUrlOpcional(imagenInicioUrl, "imagen principal"));
        restaurante.setColorPrimario(validarColor(colorPrimario));
        restaurante.setColorSecundario(validarColor(colorSecundario));
        restaurante.setColorFondo(validarColor(colorFondo));
        restaurante.setTituloInicio(tituloInicio == null || tituloInicio.isBlank()
                ? "Bienvenido a " + nombre.trim() : tituloInicio.trim());
        restaurante.setDescripcionInicio(descripcionInicio == null ? "" : descripcionInicio.trim());
        return repository.save(restaurante);
    }

    @Transactional
    public Restaurante asignarAdministrador(String slug, String correo, Usuario actor) {
        if (actor == null || !"ADMIN".equalsIgnoreCase(actor.getRol())) {
            throw new IllegalArgumentException("Solo el administrador general puede asignar responsables.");
        }
        Restaurante restaurante = repository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado."));
        Usuario responsable = usuarioRepository.findByCorreo(correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException("No existe una cuenta con ese correo. El responsable debe registrarse primero."));
        responsable.setRol("ADMIN");
        restaurante.setAdministrador(responsable);
        usuarioRepository.save(responsable);
        return repository.save(restaurante);
    }

    public void validarPermiso(Restaurante restaurante, Usuario actor) {
        if (actor == null) throw new IllegalArgumentException("Debes iniciar sesión.");
        boolean administradorGeneral = "ADMIN".equalsIgnoreCase(actor.getRol())
                && "jdptls10@gmail.com".equalsIgnoreCase(actor.getCorreo());
        boolean propietario = restaurante.getAdministrador() != null
                && restaurante.getAdministrador().getId().equals(actor.getId());
        if (!administradorGeneral && !propietario) {
            throw new IllegalArgumentException("No tienes permisos para administrar este restaurante.");
        }
    }

    private String validarColor(String valor) {
        if (valor == null || !valor.matches("^#[0-9a-fA-F]{6}$")) {
            throw new IllegalArgumentException("Los colores deben tener formato hexadecimal, por ejemplo #D6AD55.");
        }
        return valor.toUpperCase(Locale.ROOT);
    }

    private String validarUrlOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) return null;
        String url = valor.trim();
        try {
            URI uri = URI.create(url);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || url.contains("\\'") || url.contains("\\\"")
                    || url.contains("(") || url.contains(")") || url.contains("\\\\")) {
                throw new IllegalArgumentException();
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("La URL de " + campo + " debe ser una dirección HTTPS válida.");
        }
        return url;
    }

    private String normalizarSlug(String valor) {
        String sinAcentos = Normalizer.normalize(valor.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}