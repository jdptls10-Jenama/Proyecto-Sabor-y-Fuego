package com.restaurante.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Tenant principal de la plataforma. Cada restaurante tendrá su propia
 * identidad visual y su propio espacio de datos.
 */
@Entity
@Table(name = "restaurantes")
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrador_id")
    private Usuario administrador;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(name = "color_primario", nullable = false, length = 20)
    private String colorPrimario = "#D6AD55";

    @Column(name = "color_secundario", nullable = false, length = 20)
    private String colorSecundario = "#101010";

    @Column(name = "color_fondo", nullable = false, length = 20)
    private String colorFondo = "#101010";

    @Column(name = "imagen_inicio_url", length = 1000)
    private String imagenInicioUrl;

    @Column(name = "titulo_inicio", length = 180)
    private String tituloInicio;

    @Column(name = "descripcion_inicio", columnDefinition = "TEXT")
    private String descripcionInicio;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    public Restaurante() {}

    public Restaurante(String nombre, String slug) {
        this.nombre = nombre;
        this.slug = slug;
    }

    @PrePersist
    public void antesDeGuardar() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (colorPrimario == null || colorPrimario.isBlank()) colorPrimario = "#D6AD55";
        if (colorSecundario == null || colorSecundario.isBlank()) colorSecundario = "#101010";
        if (colorFondo == null || colorFondo.isBlank()) colorFondo = "#101010";
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public Usuario getAdministrador() { return administrador; }
    public void setAdministrador(Usuario administrador) { this.administrador = administrador; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getColorPrimario() { return colorPrimario; }
    public void setColorPrimario(String colorPrimario) { this.colorPrimario = colorPrimario; }
    public String getColorSecundario() { return colorSecundario; }
    public void setColorSecundario(String colorSecundario) { this.colorSecundario = colorSecundario; }
    public String getColorFondo() { return colorFondo; }
    public void setColorFondo(String colorFondo) { this.colorFondo = colorFondo; }
    public String getImagenInicioUrl() { return imagenInicioUrl; }
    public void setImagenInicioUrl(String imagenInicioUrl) { this.imagenInicioUrl = imagenInicioUrl; }
    public String getTituloInicio() { return tituloInicio; }
    public void setTituloInicio(String tituloInicio) { this.tituloInicio = tituloInicio; }
    public String getDescripcionInicio() { return descripcionInicio; }
    public void setDescripcionInicio(String descripcionInicio) { this.descripcionInicio = descripcionInicio; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}