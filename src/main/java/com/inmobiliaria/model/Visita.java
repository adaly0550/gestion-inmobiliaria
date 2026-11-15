package com.inmobiliaria.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inmobiliaria.constants.EstadoVisita;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Primera Familia: Entities - Entidad Visita (DER: VISITA).
 * Relacionada con Cliente (1:N), Propiedad (1:N) y Usuario/Asesor (1:N).
 * Punto 15: Expone tanto las relaciones JPA como los campos proyectados para el panel web.
 */
@Entity
@Table(name = "visitas")
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente", nullable = false)
    @JsonIgnore
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_propiedad", nullable = false)
    @JsonIgnore
    private Propiedad propiedad;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVisita estado = EstadoVisita.PENDIENTE;

    public Visita() {
    }

    public Visita(Cliente cliente, Propiedad propiedad, Usuario usuario,
                  LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, EstadoVisita estado) {
        this.cliente = cliente;
        this.propiedad = propiedad;
        this.usuario = usuario;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado != null ? estado : EstadoVisita.PENDIENTE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Propiedad getPropiedad() {
        return propiedad;
    }

    public void setPropiedad(Propiedad propiedad) {
        this.propiedad = propiedad;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public EstadoVisita getEstado() {
        return estado;
    }

    public void setEstado(EstadoVisita estado) {
        this.estado = estado;
    }

    // Propiedades calculadas para serialización JSON limpia en el frontend (Punto 15)
    @Transient
    public Long getIdCliente() {
        return cliente != null ? cliente.getId() : null;
    }

    @Transient
    public String getClienteNombre() {
        return cliente != null ? cliente.getNombres() : null;
    }

    @Transient
    public Long getIdPropiedad() {
        return propiedad != null ? propiedad.getId() : null;
    }

    @Transient
    public String getPropiedadCodigo() {
        return propiedad != null ? propiedad.getCodigoInterno() : null;
    }

    @Transient
    public String getPropiedadDireccion() {
        return propiedad != null ? propiedad.getDireccion() : null;
    }

    @Transient
    public Long getIdUsuario() {
        return usuario != null ? usuario.getId() : null;
    }

    @Transient
    public String getAsesorNombre() {
        return usuario != null ? usuario.getNombres() : null;
    }
}
