
package com.restaurante.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PedidoTest {

    private Usuario crearUsuario() {
        return new Usuario(
                "Cliente de prueba",
                "prueba@correo.com",
                "password123",
                "CLIENTE",
                LocalDateTime.now()
        );
    }

    @Test
    void cada_pedido_debe_tener_un_uuid_unico() {

        Pedido pedido1 = new Pedido(
                crearUsuario(), "Calle 10", "3001234567", null
        );

        Pedido pedido2 = new Pedido(
                crearUsuario(), "Calle 20", "3007654321", null
        );

        assertThat(pedido1.getTransaccionUuid()).isNotBlank();
        assertThat(pedido2.getTransaccionUuid()).isNotBlank();

        assertThat(pedido1.getTransaccionUuid())
                .isNotEqualTo(pedido2.getTransaccionUuid());

        assertThat(UUID.fromString(pedido1.getTransaccionUuid()))
                .isNotNull();
    }

    @Test
    void debe_calcular_correctamente_el_total_del_pedido() {

        Pedido pedido = new Pedido(
                crearUsuario(), "Calle 10", "3001234567", null
        );

        Producto producto1 = new Producto(
                "Hamburguesa",
                "Hamburguesa especial",
                new BigDecimal("15000.00"),
                10
        );

        Producto producto2 = new Producto(
                "Gaseosa",
                "Bebida personal",
                new BigDecimal("5000.00"),
                10
        );

        DetallePedido detalle1 = new DetallePedido(
                producto1, 2, new BigDecimal("15000.00")
        );

        DetallePedido detalle2 = new DetallePedido(
                producto2, 3, new BigDecimal("5000.00")
        );

        pedido.agregarDetalle(detalle1);
        pedido.agregarDetalle(detalle2);

        assertThat(pedido.getTotal())
                .isEqualByComparingTo("45000.00");
    }

    @Test
    void debe_asociar_los_detalles_con_el_pedido() {

        Pedido pedido = new Pedido(
                crearUsuario(), "Calle 10", "3001234567", null
        );

        Producto producto = new Producto(
                "Pizza",
                "Pizza familiar",
                new BigDecimal("30000.00"),
                5
        );

        DetallePedido detalle = new DetallePedido(
                producto, 1, new BigDecimal("30000.00")
        );

        pedido.agregarDetalle(detalle);

        assertThat(pedido.getDetalles()).hasSize(1);
        assertThat(detalle.getPedido()).isSameAs(pedido);
    }

    @Test
    void no_debe_permitir_agregar_un_detalle_nulo() {

        Pedido pedido = new Pedido(
                crearUsuario(), "Calle 10", "3001234567", null
        );

        assertThatThrownBy(() -> pedido.agregarDetalle(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
