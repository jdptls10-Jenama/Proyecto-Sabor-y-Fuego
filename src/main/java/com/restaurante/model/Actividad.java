
package com.restaurante.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "actividades")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_evento", nullable = false, length = 40)
    private String tipoEvento;

    @Column(nullable = false, length = 150)
    private String pagina;

    @Column(name = "sesion_id", length = 36)
    private String sesionId;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    @Column(length = 255)
    private String detalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Actividad() {
    }

    public Actividad(String tipoEvento, String pagina,
                     String sesionId, String detalle, Usuario usuario) {
        this.tipoEvento = tipoEvento;
        this.pagina = pagina;
        this.sesionId = sesionId;
        this.detalle = detalle;
        this.usuario = usuario;
        this.fechaEvento = LocalDateTime.now();
    }

    @PrePersist
    public void antesDeGuardar() {
        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public String getPagina() {
        return pagina;
    }

    public String getSesionId() {
        return sesionId;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }

    public String getDetalle() {
        return detalle;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public String toString() {
        return "Actividad{" +
                "id=" + id +
                ", tipoEvento='" + tipoEvento + '\'' +
                ", pagina='" + pagina + '\'' +
                ", sesionId='" + sesionId + '\'' +
                ", fechaEvento=" + fechaEvento +
                ", detalle='" + detalle + '\'' +
                ", usuarioId=" + (usuario != null ? usuario.getId() : null) +
                '}';
    }
}
