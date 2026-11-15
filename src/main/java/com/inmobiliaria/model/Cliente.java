package com.inmobiliaria.model;

import jakarta.persistence.*;

/**
 * Primera Familia: Entities - Entidad Cliente (DER: CLIENTE).
 * Representa a las personas interesadas en visitar propiedades (RN-05).
 */
@Entity
@Table(name = "clientes", indexes = {
    @Index(name = "idx_clientes_identificacion", columnList = "identificacion", unique = true)
})
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String identificacion;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String apellido;

    @Column(nullable = false)
    private String telefono;

    @Column
    private String correo;

    public Cliente() {
    }

    public Cliente(Long id, String identificacion, String nombre, String apellido, String telefono, String correo) {
        this.id = id;
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correo = correo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    // Getter compatible con el atributo 'nombres' del DER
    @Transient
    public String getNombres() {
        if (apellido == null || apellido.isBlank()) {
            return nombre != null ? nombre : "";
        }
        return ((nombre != null ? nombre : "") + " " + apellido).trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
