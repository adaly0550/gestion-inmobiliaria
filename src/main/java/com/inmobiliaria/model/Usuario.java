package com.inmobiliaria.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inmobiliaria.constants.Rol;
import jakarta.persistence.*;

/**
 * Primera Familia: Entities - Entidad Usuario (DER: USUARIO).
 * Punto 15: La contraseña nunca se expone en las respuestas JSON (WRITE_ONLY).
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false, unique = true)
    private String usuario;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "contrasena", nullable = false)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    public Usuario() {
    }

    public Usuario(Long id, String nombres, String usuario, String contrasena, Rol rol) {
        this.id = id;
        this.nombres = nombres;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
