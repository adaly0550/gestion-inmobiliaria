package com.inmobiliaria.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Primera Familia: Entities - Entidad Aviso (Punto 20 / HU-11 / RF-13 / RN-10).
 * Almacena las notificaciones internas enviadas a los asesores cuando se agenda,
 * cancela o reprograma una visita.
 */
@Entity
@Table(name = "avisos")
public class Aviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    public Aviso() {
    }

    public Aviso(Usuario usuario, String mensaje, LocalDateTime fechaHora) {
        this.usuario = usuario;
        this.mensaje = mensaje;
        this.fechaHora = fechaHora != null ? fechaHora : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    @Transient
    public Long getIdUsuario() {
        return usuario != null ? usuario.getId() : null;
    }

    @Transient
    public String getUsuarioNombre() {
        return usuario != null ? usuario.getNombres() : null;
    }
}
