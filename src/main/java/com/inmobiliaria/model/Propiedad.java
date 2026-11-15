package com.inmobiliaria.model;

import com.inmobiliaria.constants.EstadoPropiedad;
import com.inmobiliaria.constants.Modalidad;
import jakarta.persistence.*;

/**
 * Primera Familia: Entities - Entidad Propiedad (DER: PROPIEDAD).
 * Incluye el campo descripcion (Punto 4 / HU-01 / RF-01).
 */
@Entity
@Table(name = "propiedades")
public class Propiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigoInterno;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modalidad modalidad;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPropiedad estado = EstadoPropiedad.DISPONIBLE;

    public Propiedad() {
    }

    public Propiedad(Long id, String codigoInterno, String direccion, String tipo,
                     Modalidad modalidad, String descripcion, EstadoPropiedad estado) {
        this.id = id;
        this.codigoInterno = codigoInterno;
        this.direccion = direccion;
        this.tipo = tipo;
        this.modalidad = modalidad;
        this.descripcion = descripcion;
        this.estado = estado != null ? estado : EstadoPropiedad.DISPONIBLE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public void setCodigoInterno(String codigoInterno) {
        this.codigoInterno = codigoInterno;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoPropiedad getEstado() {
        return estado;
    }

    public void setEstado(EstadoPropiedad estado) {
        this.estado = estado;
    }
}
