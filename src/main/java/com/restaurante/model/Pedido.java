
package com.restaurante.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaccion_uuid", nullable = false, unique = true, length = 36)
    private String transaccionUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDateTime fechaPedido;

    @Column(nullable = false, length = 30)
    private String estado = "PENDIENTE";

    @Column(name = "estado_pago", nullable = false, length = 30)
    private String estadoPago = "PENDIENTE";

    @Column(name = "direccion_entrega", nullable = false, length = 250)
    private String direccionEntrega;

    @Column(name = "telefono_contacto", nullable = false, length = 30)
    private String telefonoContacto;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(
            mappedBy = "pedido",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
    }

    public Pedido(
            Usuario usuario,
            String direccionEntrega,
            String telefonoContacto,
            String observaciones
    ) {
        this.usuario = usuario;
        this.direccionEntrega = direccionEntrega;
        this.telefonoContacto = telefonoContacto;
        this.observaciones = observaciones;
        this.fechaPedido = LocalDateTime.now();
        this.estado = "PENDIENTE";
        this.estadoPago = "PENDIENTE";
        this.total = BigDecimal.ZERO;
        this.transaccionUuid = generarIdentificadorTransaccion();
    }

    /**
     * Genera un identificador único para cada transacción.
     */
    public static String generarIdentificadorTransaccion() {
        return UUID.randomUUID().toString();
    }

    @PrePersist
    public void antesDeGuardar() {

        if (transaccionUuid == null || transaccionUuid.isBlank()) {
            transaccionUuid = generarIdentificadorTransaccion();
        }

        if (fechaPedido == null) {
            fechaPedido = LocalDateTime.now();
        }

        if (estado == null) {
            estado = "PENDIENTE";
        }

        if (estadoPago == null) {
            estadoPago = "PENDIENTE";
        }

        if (total == null) {
            total = BigDecimal.ZERO;
        }
    }

    public void agregarDetalle(DetallePedido detalle) {

        if (detalle == null) {
            throw new IllegalArgumentException(
                    "El detalle del pedido no puede ser nulo"
            );
        }

        detalles.add(detalle);
        detalle.setPedido(this);
        recalcularTotal();
    }

    public void recalcularTotal() {

        BigDecimal suma = BigDecimal.ZERO;

        for (DetallePedido detalle : detalles) {
            suma = suma.add(detalle.getSubtotal());
        }

        this.total = suma;
    }

    public void aprobarPagoPrueba() {
        this.estadoPago = "PAGADO";
    }

    public Long getId() {
        return id;
    }

    public String getTransaccionUuid() {
        return transaccionUuid;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", transaccionUuid='" + transaccionUuid + '\'' +
                ", usuarioId=" + (usuario != null ? usuario.getId() : null) +
                ", fechaPedido=" + fechaPedido +
                ", estado='" + estado + '\'' +
                ", direccionEntrega='" + direccionEntrega + '\'' +
                ", telefonoContacto='" + telefonoContacto + '\'' +
                ", total=" + total +
                ", cantidadDetalles=" + detalles.size() +
                '}';
    }
}
