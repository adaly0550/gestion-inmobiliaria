package com.inmobiliaria.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inmobiliaria.constants.TipoCambio;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Primera Familia: Entities - Entidad HistorialVisita (DER: HISTORIAL_VISITA).
 * Guarda cada cancelación o reprogramación con fecha y hora completas (Punto 6 / RF-11 / RN-08).
 */
@Entity
@Table(name = "historial_visitas")
public class HistorialVisita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_visita", nullable = false)
    @JsonIgnore
    private Visita visita;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cambio", nullable = false)
    private TipoCambio tipoCambio;

    @Column(nullable = false)
    private String motivo;

    @Column(name = "fecha_hora_cambio", nullable = false)
    private LocalDateTime fechaHoraCambio;

    // Formato "YYYY-MM-DD HH:MM" (Punto 6: fecha y hora anterior y nueva)
    @Column(name = "fecha_anterior")
    private String fechaAnterior;

    @Column(name = "fecha_nueva")
    private String fechaNueva;

    public HistorialVisita() {
    }

    public HistorialVisita(Visita visita, Usuario usuario, TipoCambio tipoCambio,
                           String motivo, LocalDateTime fechaHoraCambio,
                           String fechaAnterior, String fechaNueva) {
        this.visita = visita;
        this.usuario = usuario;
        this.tipoCambio = tipoCambio;
        this.motivo = motivo;
        this.fechaHoraCambio = fechaHoraCambio != null ? fechaHoraCambio : LocalDateTime.now();
        this.fechaAnterior = fechaAnterior;
        this.fechaNueva = fechaNueva;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Visita getVisita() {
        return visita;
    }

    public void setVisita(Visita visita) {
        this.visita = visita;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoCambio getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(TipoCambio tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFechaHoraCambio() {
        return fechaHoraCambio;
    }

    public void setFechaHoraCambio(LocalDateTime fechaHoraCambio) {
        this.fechaHoraCambio = fechaHoraCambio;
    }

    public String getFechaAnterior() {
        return fechaAnterior;
    }

    public void setFechaAnterior(String fechaAnterior) {
        this.fechaAnterior = fechaAnterior;
    }

    public String getFechaNueva() {
        return fechaNueva;
    }

    public void setFechaNueva(String fechaNueva) {
        this.fechaNueva = fechaNueva;
    }

    // Campos proyectados para la vista de Auditoría / Historial (Punto 15)
    @Transient
    public Long getIdVisita() {
        return visita != null ? visita.getId() : null;
    }

    @Transient
    public Long getIdUsuario() {
        return usuario != null ? usuario.getId() : null;
    }

    @Transient
    public String getUsuarioNombre() {
        return usuario != null ? usuario.getNombres() : null;
    }

    @Transient
    public String getUsuarioRol() {
        return usuario != null && usuario.getRol() != null ? usuario.getRol().name() : null;
    }

    @Transient
    public String getPropiedadCodigo() {
        return visita != null && visita.getPropiedad() != null
                ? visita.getPropiedad().getCodigoInterno()
                : null;
    }
}
