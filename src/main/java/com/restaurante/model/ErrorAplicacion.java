
package com.restaurante.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "errores_aplicacion")
public class ErrorAplicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_error", nullable = false)
    private LocalDateTime fechaError;

    @Column(name = "tipo_error", nullable = false, length = 150)
    private String tipoError;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "detalle_tecnico", columnDefinition = "TEXT")
    private String detalleTecnico;

    private String ruta;
    private String metodoHttp;
    private Integer codigoHttp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "sesion_id")
    private String sesionId;

    private String estado = "PENDIENTE";

    @Column(name = "fecha_solucion")
    private LocalDateTime fechaSolucion;

    @Column(columnDefinition = "TEXT")
    private String solucion;

    public ErrorAplicacion() {
    }

    @PrePersist
    public void antesDeGuardar() {
        if (fechaError == null) {
            fechaError = LocalDateTime.now();
        }

        if (estado == null) {
            estado = "PENDIENTE";
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFechaError() {
        return fechaError;
    }

    public String getTipoError() {
        return tipoError;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getDetalleTecnico() {
        return detalleTecnico;
    }

    public String getRuta() {
        return ruta;
    }

    public String getMetodoHttp() {
        return metodoHttp;
    }

    public Integer getCodigoHttp() {
        return codigoHttp;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getSesionId() {
        return sesionId;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFechaSolucion() {
        return fechaSolucion;
    }

    public String getSolucion() {
        return solucion;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setFechaSolucion(LocalDateTime fechaSolucion) {
        this.fechaSolucion = fechaSolucion;
    }

    public void setSolucion(String solucion) {
        this.solucion = solucion;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public void setMetodoHttp(String metodoHttp) {
        this.metodoHttp = metodoHttp;
    }

    public void setCodigoHttp(Integer codigoHttp) {
        this.codigoHttp = codigoHttp;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setSesionId(String sesionId) {
        this.sesionId = sesionId;
    }

    public void setTipoError(String tipoError) {
        this.tipoError = tipoError;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public void setDetalleTecnico(String detalleTecnico) {
        this.detalleTecnico = detalleTecnico;
    }

    @Override
    public String toString() {
        return "ErrorAplicacion{" +
                "id=" + id +
                ", fechaError=" + fechaError +
                ", tipoError='" + tipoError + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", ruta='" + ruta + '\'' +
                ", metodoHttp='" + metodoHttp + '\'' +
                ", codigoHttp=" + codigoHttp +
                ", usuarioId=" + (usuario != null ? usuario.getId() : null) +
                ", sesionId='" + sesionId + '\'' +
                ", estado='" + estado + '\'' +
                ", fechaSolucion=" + fechaSolucion +
                ", solucion='" + solucion + '\'' +
                '}';
    }
}
