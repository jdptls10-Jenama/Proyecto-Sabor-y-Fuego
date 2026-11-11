
package com.restaurante.service;

import com.restaurante.model.DetallePedido;
import com.restaurante.model.Pedido;
import com.restaurante.model.Producto;
import com.restaurante.model.Usuario;
import com.restaurante.repository.PedidoRepository;
import com.restaurante.repository.ProductoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    // Crear un pedido y descontar las existencias
    @Transactional
    public Pedido crearPedido(
            Usuario usuario,
            String direccion,
            String telefono,
            String observaciones,
            Map<Long, Integer> carrito) {

        validarPedido(usuario, direccion, telefono, carrito);

        Pedido pedido = new Pedido(
                usuario,
                direccion.trim(),
                telefono.trim(),
                observaciones
        );

        for (Map.Entry<Long, Integer> entrada : carrito.entrySet()) {

            Long idProducto = entrada.getKey();
            Integer cantidad = entrada.getValue();

            if (idProducto == null || cantidad == null || cantidad <= 0) {
                throw new IllegalArgumentException(
                        "Los datos del producto o su cantidad no son válidos."
                );
            }

            Producto producto = productoRepository.findById(idProducto)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "No se encontró el producto con ID: " + idProducto
                            )
                    );

            validarProducto(producto, cantidad);

            BigDecimal precioUnitario = producto.getPrecio();

            DetallePedido detalle = new DetallePedido(
                    producto,
                    cantidad,
                    precioUnitario
            );

            pedido.agregarDetalle(detalle);

            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);
        }

        pedido.recalcularTotal();

        return pedidoRepository.save(pedido);
    }

    // Consultar todos los pedidos
    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos() {

        return pedidoRepository.findAllByOrderByFechaPedidoDesc();
    }

    // Consultar pedidos por estado
    @Transactional(readOnly = true)
    public List<Pedido> listarPedidosPorEstado(String estado) {

        if (estado == null || estado.isBlank()
                || "TODOS".equalsIgnoreCase(estado)) {
            return listarPedidos();
        }

        return pedidoRepository.findByEstadoOrderByFechaPedidoDesc(
                estado.trim().toUpperCase(Locale.ROOT)
        );
    }

    // Consultar historial de pedidos de un cliente
    @Transactional(readOnly = true)
    public List<Pedido> listarPedidosPorUsuario(Long usuarioId) {

        if (usuarioId == null) {
            throw new IllegalArgumentException(
                    "El identificador del usuario no puede estar vacío."
            );
        }

        return pedidoRepository.findByUsuario_IdOrderByFechaPedidoDesc(
                usuarioId
        );
    }

    // Buscar un pedido por su identificador
    @Transactional(readOnly = true)
    public Pedido buscarPedido(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del pedido no puede estar vacío."
            );
        }

        return pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el pedido."
                        )
                );
    }

    // Actualizar el estado de un pedido
    @Transactional
    public Pedido actualizarEstado(Long pedidoId, String nuevoEstado) {

        if (pedidoId == null) {
            throw new IllegalArgumentException(
                    "El identificador del pedido no puede estar vacío."
            );
        }

        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un estado."
            );
        }

        String estado = nuevoEstado.trim().toUpperCase(Locale.ROOT);

        if (!List.of(
                "PENDIENTE",
                "EN_PREPARACION",
                "EN_CAMINO",
                "ENTREGADO"
        ).contains(estado)) {
            throw new IllegalArgumentException(
                    "El estado seleccionado no es válido."
            );
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el pedido."
                        )
                );

        pedido.setEstado(estado);

        return pedidoRepository.save(pedido);
    }

    // Validar los datos generales del pedido
    private void validarPedido(
            Usuario usuario,
            String direccion,
            String telefono,
            Map<Long, Integer> carrito) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "Debes iniciar sesión para realizar un pedido."
            );
        }

        if (direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes escribir una dirección de entrega."
            );
        }

        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes escribir un teléfono de contacto."
            );
        }

        if (carrito == null || carrito.isEmpty()) {
            throw new IllegalArgumentException(
                    "No puedes realizar un pedido con el carrito vacío."
            );
        }
    }

    // Validar precio y existencias del producto
    private void validarProducto(Producto producto, Integer cantidad) {

        if (producto.getPrecio() == null
                || producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El precio del producto no es válido."
            );
        }

        if (producto.getStock() == null
                || producto.getStock() < cantidad) {
            throw new IllegalArgumentException(
                    "No hay suficientes existencias de "
                            + producto.getNombre()
            );
        }
    }

    // Aprobar el pago de prueba
    @Transactional
    public Pedido aprobarPagoPrueba(Long pedidoId) {

        if (pedidoId == null) {
            throw new IllegalArgumentException(
                    "El identificador del pedido no puede estar vacío."
            );
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el pedido."
                        )
                );

        if ("PAGADO".equals(pedido.getEstadoPago())) {
            throw new IllegalArgumentException(
                    "Este pedido ya está pagado."
            );
        }

        pedido.aprobarPagoPrueba();

        return pedidoRepository.save(pedido);
    }
}
