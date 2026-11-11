
package com.restaurante.service;

import com.restaurante.model.Usuario;
import com.restaurante.repository.UsuarioRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ActividadService actividadService;

    private final PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ActividadService actividadService) {

        this.usuarioRepository = usuarioRepository;
        this.actividadService = actividadService;
    }

    public Usuario registrar(String nombre, String correo,
                             String password) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio"
            );
        }

        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo es obligatorio"
            );
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener mínimo 8 caracteres"
            );
        }

        String correoNormalizado =
                correo.trim().toLowerCase();

        if (usuarioRepository.existsByCorreo(correoNormalizado)) {
            throw new IllegalArgumentException(
                    "Este correo ya está registrado"
            );
        }

        String passwordCifrado =
                passwordEncoder.encode(password);

        String rol = correoNormalizado.equals("jdptls10@gmail.com")
                ? "ADMIN"
                : "CLIENTE";

        Usuario usuario = new Usuario(
                nombre.trim(),
                correoNormalizado,
                passwordCifrado,
                rol
        );

        Usuario guardado = usuarioRepository.save(usuario);

        actividadService.registrar(
                "REGISTRO",
                guardado,
                "registro",
                null,
                "Nueva cuenta registrada"
        );

        return guardado;
    }

    public Optional<Usuario> iniciarSesion(
            String correo, String password) {

        if (correo == null || password == null
                || correo.isBlank()) {

            actividadService.registrar(
                    "LOGIN_FALLIDO",
                    null,
                    "login",
                    null,
                    "Intento de inicio de sesión con datos incompletos"
            );

            return Optional.empty();
        }

        Optional<Usuario> encontrado =
                usuarioRepository.findByCorreo(
                        correo.trim().toLowerCase()
                );

        if (encontrado.isPresent()
                && passwordEncoder.matches(
                password,
                encontrado.get().getPassword()
        )) {

            actividadService.registrar(
                    "LOGIN_EXITOSO",
                    encontrado.get(),
                    "login",
                    null,
                    "Inicio de sesión correcto"
            );

            return encontrado;
        }

        actividadService.registrar(
                "LOGIN_FALLIDO",
                null,
                "login",
                null,
                "Credenciales incorrectas"
        );

        return Optional.empty();
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {

        if (correo == null || correo.isBlank()) {
            return Optional.empty();
        }

        return usuarioRepository.findByCorreo(
                correo.trim().toLowerCase()
        );
    }

    // Listar todos los clientes registrados
    public List<Usuario> listarClientes() {

        return usuarioRepository.findByRolIgnoreCaseOrderByNombreAsc(
                "CLIENTE"
        );
    }

    // Buscar clientes por nombre o correo
    public List<Usuario> buscarClientes(String texto) {

        if (texto == null || texto.isBlank()) {
            return listarClientes();
        }

        String busqueda = texto.trim();

        return usuarioRepository
                .findByRolIgnoreCaseAndNombreContainingIgnoreCaseOrRolIgnoreCaseAndCorreoContainingIgnoreCase(
                        "CLIENTE",
                        busqueda,
                        "CLIENTE",
                        busqueda
                );
    }
}
