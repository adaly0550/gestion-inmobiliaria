package com.inmobiliaria.service;

import com.inmobiliaria.model.HistorialVisita;
import com.inmobiliaria.model.Usuario;
import com.inmobiliaria.model.Visita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Tercera Familia: Service (Interface) - VisitaService.
 * Define las 8 operaciones del contrato del módulo Visitas según el diagrama del PDF,
 * además de la consulta de horarios libres (Punto 31 / HU-05).
 */
public interface VisitaService {

    Visita agendarVisita(Map<String, Object> dto, Usuario usuarioSesion);

    Visita cancelarVisita(Long idVisita, String motivo, Usuario usuarioSesion);

    Visita reprogramarVisita(Long idVisita, LocalDate nuevaFecha, LocalTime nuevaHora, String motivo, Usuario usuarioSesion);

    void cancelarVisitasPorPropiedad(Long idPropiedad, String motivo, Usuario usuarioResponsable);

    List<Visita> listarPendientes(LocalDate fecha, Long idAsesor, Usuario usuarioSesion);

    List<HistorialVisita> consultarHistorial(Long idVisita, Usuario usuarioSesion);

    List<Visita> listarPorClienteOPropiedad(Long idCliente, Long idPropiedad, Usuario usuarioSesion);

    void generarAvisoInterno(Visita visita, String mensaje);

    List<Visita> listarTodas(LocalDate fecha, Usuario usuarioSesion);

    List<String> obtenerHorariosLibres(LocalDate fecha, Long idPropiedad, Long idCliente, Long idAsesor, Long idVisitaIgnorada, Usuario usuarioSesion);
}
