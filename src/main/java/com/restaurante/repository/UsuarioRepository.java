
package com.restaurante.repository;

import com.restaurante.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<Usuario> findByRolIgnoreCaseOrderByNombreAsc(String rol);

    List<Usuario> findByRolIgnoreCaseAndNombreContainingIgnoreCaseOrRolIgnoreCaseAndCorreoContainingIgnoreCase(
            String rol1,
            String nombre,
            String rol2,
            String correo
    );
}
