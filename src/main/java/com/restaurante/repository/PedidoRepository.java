package com.restaurante.repository;

import com.restaurante.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    List<Pedido> findAllByRestaurante_IdOrderByFechaPedidoDesc(Long restauranteId);
    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    List<Pedido> findByRestaurante_IdAndEstadoOrderByFechaPedidoDesc(Long restauranteId, String estado);
    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    Optional<Pedido> findByIdAndRestaurante_Id(Long pedidoId, Long restauranteId);
    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    List<Pedido> findByUsuario_IdAndRestaurante_IdOrderByFechaPedidoDesc(Long usuarioId, Long restauranteId);
    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    List<Pedido> findByUsuario_IdOrderByFechaPedidoDesc(Long usuarioId);
    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    List<Pedido> findByEstadoOrderByFechaPedidoDesc(String estado);
    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    List<Pedido> findAllByOrderByFechaPedidoDesc();
    @Override
    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    Optional<Pedido> findById(Long id);
}