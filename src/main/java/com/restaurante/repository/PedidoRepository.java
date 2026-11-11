
package com.restaurante.repository;

import com.restaurante.model.Pedido;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Consultar pedidos de un cliente
    @EntityGraph(attributePaths = {
            "usuario",
            "detalles",
            "detalles.producto"
    })
    List<Pedido> findByUsuario_IdOrderByFechaPedidoDesc(Long usuarioId);

    // Consultar pedidos según su estado
    @EntityGraph(attributePaths = {
            "usuario",
            "detalles",
            "detalles.producto"
    })
    List<Pedido> findByEstadoOrderByFechaPedidoDesc(String estado);

    // Consultar todos los pedidos, del más reciente al más antiguo
    @EntityGraph(attributePaths = {
            "usuario",
            "detalles",
            "detalles.producto"
    })
    List<Pedido> findAllByOrderByFechaPedidoDesc();

    // Consultar un pedido con sus detalles
    @Override
    @EntityGraph(attributePaths = {
            "usuario",
            "detalles",
            "detalles.producto"
    })
    Optional<Pedido> findById(Long id);
}
