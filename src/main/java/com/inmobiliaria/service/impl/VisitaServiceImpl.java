package com.inmobiliaria.service.impl;

import com.inmobiliaria.constants.*;
import com.inmobiliaria.exception.AppException;
import com.inmobiliaria.model.*;
import com.inmobiliaria.repository.*;
import com.inmobiliaria.service.VisitaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Cuarta Familia: Implementación - VisitaServiceImpl.
 * Se asocia con los 5 Repository del DER (VisitaRepository, PropiedadRepository,
 * ClienteRepository, UsuarioRepository, HistorialVisitaRepository) más AvisoRepository.
 * Incluye todos los métodos públicos del contrato y los 5 métodos privados del diagrama:
 * validarDisponibilidad(), calcularHoraFin(), validarCruceHorario(), validarAsesor() y registrarHistorial().
 */
@Service
public class VisitaServiceImpl implements VisitaService {

    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final VisitaRepository visitaRepository;
    private final PropiedadRepository propiedadRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialVisitaRepository historialVisitaRepository;
    private final AvisoRepository avisoRepository;

    public VisitaServiceImpl(VisitaRepository visitaRepository,
                             PropiedadRepository propiedadRepository,
                             ClienteRepository clienteRepository,
                             UsuarioRepository usuarioRepository,
                             HistorialVisitaRepository historialVisitaRepository,
                             AvisoRepository avisoRepository) {
        this.visitaRepository = visitaRepository;
        this.propiedadRepository = propiedadRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialVisitaRepository = historialVisitaRepository;
        this.avisoRepository = avisoRepository;
    }

    // 1. HU-05 / RF-05, RF-06, RF-07: Agendar visita
    @Override
    @Transactional
    public Visita agendarVisita(Map<String, Object> dto, Usuario usuarioSesion) {
        Long idCliente = parseLong(dto.get("idCliente"));
        Long idPropiedad = parseLong(dto.get("idPropiedad"));
        Long idAsesorDto = parseLong(dto.get("idAsesor") != null ? dto.get("idAsesor") : dto.get("idUsuario"));
        String fechaStr = dto.get("fecha") != null ? String.valueOf(dto.get("fecha")).trim() : null;
        String horaStr = dto.get("horaInicio") != null ? String.valueOf(dto.get("horaInicio")).trim() : null;

        // RN-03: Cada visita debe tener cliente, propiedad, asesor, fecha y hora
        if (idCliente == null || idPropiedad == null || fechaStr == null || fechaStr.isBlank() || horaStr == null || horaStr.isBlank()) {
            throw new AppException("RN-03: Cliente, propiedad, fecha y hora de inicio son obligatorios.", 400);
        }

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new AppException("El cliente seleccionado no existe.", 404));

        Propiedad propiedad = validarDisponibilidad(idPropiedad);
        Usuario asesorAsignado = validarAsesor(idAsesorDto, usuarioSesion);

        LocalDate fecha = parseFecha(fechaStr);
        LocalTime horaInicio = parseHora(horaStr);
        LocalTime horaFin = calcularHoraFin(horaInicio);

        validarHorarioAtencion(fecha, horaInicio, horaFin);
        validarCruceHorario(asesorAsignado.getId(), cliente.getId(), propiedad.getId(), fecha, horaInicio, horaFin, null);

        Visita nuevaVisita = new Visita(
                cliente,
                propiedad,
                asesorAsignado,
                fecha,
                horaInicio,
                horaFin,
                EstadoVisita.PENDIENTE
        );

        Visita guardada = visitaRepository.save(nuevaVisita);

        // RN-10 / RF-13: Generar aviso interno al asesor asignado
        generarAvisoInterno(
                guardada,
                String.format("Nueva visita agendada (%s) para el %s de %s a %s.",
                        propiedad.getCodigoInterno(),
                        fecha,
                        horaInicio.format(HORA_FMT),
                        horaFin.format(HORA_FMT))
        );

        return guardada;
    }

    // 2. HU-06 / RF-08: Cancelar visita
    @Override
    @Transactional
    public Visita cancelarVisita(Long idVisita, String motivo, Usuario usuarioSesion) {
        if (motivo == null || motivo.isBlank()) {
            throw new AppException("RN-08: Se requiere un motivo obligatorio para cancelar la visita.", 400);
        }

        Visita visita = visitaRepository.findById(idVisita)
                .orElseThrow(() -> new AppException("Visita no encontrada.", 404));

        if (visita.getEstado() == EstadoVisita.CANCELADA) {
            throw new AppException("La visita ya se encuentra cancelada.", 400);
        }

        // RN-07: Un asesor solo puede cancelar sus propias visitas
        if (usuarioSesion.getRol() == Rol.ASESOR && !Objects.equals(visita.getIdUsuario(), usuarioSesion.getId())) {
            throw new AppException("RN-07: Un asesor solo puede cancelar sus propias visitas.", 403);
        }

        String fechaHoraAnterior = formatearFechaHora(visita.getFecha(), visita.getHoraInicio());

        // RNF-03: Nunca se borra la visita, solo cambia su estado a CANCELADA
        visita.setEstado(EstadoVisita.CANCELADA);
        Visita actualizada = visitaRepository.save(visita);

        // Punto 6 / RF-11: Guardar historial con fecha y hora anterior ("YYYY-MM-DD HH:MM") y fechaNueva null
        registrarHistorial(actualizada, usuarioSesion, TipoCambio.CANCELACION, motivo.trim(), fechaHoraAnterior, null);

        // RN-10: Aviso interno al asesor dueño de la visita
        generarAvisoInterno(actualizada, "Visita #" + idVisita + " cancelada. Motivo: " + motivo.trim());

        return actualizada;
    }

    // 3. Punto 1 / HU-06 / RF-08: Reprogramar visita
    @Override
    @Transactional
    public Visita reprogramarVisita(Long idVisita, LocalDate nuevaFecha, LocalTime nuevaHoraInicio, String motivo, Usuario usuarioSesion) {
        // 1. Exigir motivo no vacío (RN-08)
        if (motivo == null || motivo.isBlank()) {
            throw new AppException("RN-08: Se requiere un motivo obligatorio para reprogramar la visita.", 400);
        }
        if (nuevaFecha == null || nuevaHoraInicio == null) {
            throw new AppException("RN-03: Debe indicar la nueva fecha y la nueva hora de inicio para reprogramar.", 400);
        }

        // 2. La visita debe existir y estar PENDIENTE
        Visita visita = visitaRepository.findById(idVisita)
                .orElseThrow(() -> new AppException("Visita no encontrada.", 404));

        if (visita.getEstado() != EstadoVisita.PENDIENTE) {
            throw new AppException("Solo se pueden reprogramar visitas en estado PENDIENTE.", 400);
        }

        // 3. Un asesor solo reprograma las suyas (RN-07)
        if (usuarioSesion.getRol() == Rol.ASESOR && !Objects.equals(visita.getIdUsuario(), usuarioSesion.getId())) {
            throw new AppException("RN-07: Un asesor solo puede reprogramar sus propias visitas.", 403);
        }

        // 4. La propiedad debe seguir DISPONIBLE (RN-01)
        validarDisponibilidad(visita.getIdPropiedad());

        // 5. Recalcular horaFin = horaInicio + 60 y validar horario de atención
        LocalTime nuevaHoraFin = calcularHoraFin(nuevaHoraInicio);
        validarHorarioAtencion(nuevaFecha, nuevaHoraInicio, nuevaHoraFin);

        // 6 y 7. Revisar cruces excluyendo la propia visita (v.id !== idVisita) sobre el asesor dueño (visita.idUsuario)
        validarCruceHorario(
                visita.getIdUsuario(),
                visita.getIdCliente(),
                visita.getIdPropiedad(),
                nuevaFecha,
                nuevaHoraInicio,
                nuevaHoraFin,
                visita.getId()
        );

        String fechaHoraAnterior = formatearFechaHora(visita.getFecha(), visita.getHoraInicio());
        String fechaHoraNueva = formatearFechaHora(nuevaFecha, nuevaHoraInicio);

        visita.setFecha(nuevaFecha);
        visita.setHoraInicio(nuevaHoraInicio);
        visita.setHoraFin(nuevaHoraFin);
        Visita actualizada = visitaRepository.save(visita);

        // 8. Guardar en el historial con tipo REPROGRAMACION, usuario en sesión, fechaAnterior y fechaNueva
        registrarHistorial(
                actualizada,
                usuarioSesion,
                TipoCambio.REPROGRAMACION,
                motivo.trim(),
                fechaHoraAnterior,
                fechaHoraNueva
        );

        // 9. Enviar aviso al asesor dueño (RN-10)
        generarAvisoInterno(
                actualizada,
                String.format("Visita #%d reprogramada de [%s] a [%s]. Motivo: %s",
                        idVisita, fechaHoraAnterior, fechaHoraNueva, motivo.trim())
        );

        return actualizada;
    }

    // 4. Punto 7 / HU-07 / RF-09 / RN-02: Cancelación en cascada solo de visitas futuras pendientes
    @Override
    @Transactional
    public void cancelarVisitasPorPropiedad(Long idPropiedad, String motivo, Usuario usuarioResponsable) {
        List<Visita> pendientes = visitaRepository.findByPropiedadIdAndEstado(idPropiedad, EstadoVisita.PENDIENTE);
        LocalDateTime ahora = LocalDateTime.now();

        for (Visita v : pendientes) {
            LocalDateTime inicioVisita = LocalDateTime.of(v.getFecha(), v.getHoraInicio());
            // Solo cancelar visitas futuras pendientes (Punto 7)
            if (inicioVisita.isAfter(ahora)) {
                String fechaHoraAnterior = formatearFechaHora(v.getFecha(), v.getHoraInicio());
                v.setEstado(EstadoVisita.CANCELADA);
                visitaRepository.save(v);

                Usuario responsable = usuarioResponsable != null ? usuarioResponsable : v.getUsuario();
                registrarHistorial(v, responsable, TipoCambio.CANCELACION, motivo, fechaHoraAnterior, null);
                generarAvisoInterno(v, "Visita #" + v.getId() + " cancelada automáticamente: " + motivo);
            }
        }
    }

    // 5. Punto 5 / HU-08 / RF-10: Consultar visitas pendientes por fecha o por asesor
    @Override
    @Transactional(readOnly = true)
    public List<Visita> listarPendientes(LocalDate fecha, Long idAsesor, Usuario usuarioSesion) {
        List<Visita> pendientes = visitaRepository.findByEstadoOrderByFechaAscHoraInicioAsc(EstadoVisita.PENDIENTE);

        // RN-07: Si el usuario es ASESOR, se ignora el parámetro idAsesor y se fuerza su propio id
        Long filtroAsesorId = (usuarioSesion.getRol() == Rol.ASESOR) ? usuarioSesion.getId() : idAsesor;

        return pendientes.stream()
                .filter(v -> filtroAsesorId == null || Objects.equals(v.getIdUsuario(), filtroAsesorId))
                .filter(v -> fecha == null || Objects.equals(v.getFecha(), fecha))
                .toList();
    }

    // 6. Punto 2 / HU-09 / RF-11: Consultar historial de una visita específica (solo Administrador)
    @Override
    @Transactional(readOnly = true)
    public List<HistorialVisita> consultarHistorial(Long idVisita, Usuario usuarioSesion) {
        if (usuarioSesion.getRol() != Rol.ADMINISTRADOR) {
            throw new AppException("HU-09: Acceso denegado. Solo el administrador puede consultar el historial de una visita.", 403);
        }

        if (!visitaRepository.existsById(idVisita)) {
            throw new AppException("Visita no encontrada.", 404);
        }

        return historialVisitaRepository.findByVisitaId(idVisita);
    }

    // 7. Punto 3 / HU-10 / RF-12: Consultar todas las visitas de un cliente o de una propiedad (incluye canceladas)
    @Override
    @Transactional(readOnly = true)
    public List<Visita> listarPorClienteOPropiedad(Long idCliente, Long idPropiedad, Usuario usuarioSesion) {
        if (usuarioSesion.getRol() != Rol.ADMINISTRADOR) {
            throw new AppException("HU-10: Acceso denegado. Solo el administrador puede consultar el seguimiento por cliente o propiedad.", 403);
        }

        if (idCliente != null && idPropiedad != null) {
            return visitaRepository.findAllByOrderByIdDesc().stream()
                    .filter(v -> Objects.equals(v.getIdCliente(), idCliente) && Objects.equals(v.getIdPropiedad(), idPropiedad))
                    .toList();
        }
        if (idCliente != null) {
            return visitaRepository.findByClienteId(idCliente);
        }
        if (idPropiedad != null) {
            return visitaRepository.findByPropiedadId(idPropiedad);
        }
        return visitaRepository.findAllByOrderByIdDesc();
    }

    // 8. RN-10 / RF-13: Generar aviso interno (sin console.log según Punto 28)
    @Override
    @Transactional
    public void generarAvisoInterno(Visita visita, String mensaje) {
        if (visita != null && visita.getUsuario() != null) {
            Aviso aviso = new Aviso(visita.getUsuario(), mensaje, LocalDateTime.now());
            avisoRepository.save(aviso);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Visita> listarTodas(LocalDate fecha, Usuario usuarioSesion) {
        List<Visita> todas = visitaRepository.findAllByOrderByIdDesc();
        return todas.stream()
                .filter(v -> usuarioSesion.getRol() != Rol.ASESOR || Objects.equals(v.getIdUsuario(), usuarioSesion.getId()))
                .filter(v -> fecha == null || Objects.equals(v.getFecha(), fecha))
                .toList();
    }

    // Punto 31 / HU-05: Obtener horarios libres de 60 minutos dentro del horario de atención
    @Override
    @Transactional(readOnly = true)
    public List<String> obtenerHorariosLibres(LocalDate fecha, Long idPropiedad, Long idCliente,
                                              Long idAsesor, Long idVisitaIgnorada, Usuario usuarioSesion) {
        if (fecha == null || fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of();
        }

        Long asesorIdEfectivo = (usuarioSesion.getRol() == Rol.ASESOR)
                ? usuarioSesion.getId()
                : (idAsesor != null ? idAsesor : usuarioSesion.getId());

        int horaCierre = (fecha.getDayOfWeek() == DayOfWeek.SATURDAY) ? 13 : 18;
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        List<Visita> visitasAsesor = asesorIdEfectivo != null
                ? visitaRepository.findByUsuarioIdAndFecha(asesorIdEfectivo, fecha)
                : List.of();
        List<Visita> visitasCliente = idCliente != null
                ? visitaRepository.findByClienteIdAndFecha(idCliente, fecha)
                : List.of();
        List<Visita> visitasPropiedad = idPropiedad != null
                ? visitaRepository.findByPropiedadIdAndFecha(idPropiedad, fecha)
                : List.of();

        List<String> libres = new ArrayList<>();
        for (int h = 9; h < horaCierre; h++) {
            LocalTime inicio = LocalTime.of(h, 0);
            LocalTime fin = inicio.plusHours(1);

            if (fecha.isBefore(hoy) || (fecha.isEqual(hoy) && !inicio.isAfter(ahora))) {
                continue;
            }

            if (!seCruza(visitasAsesor, inicio, fin, idVisitaIgnorada)
                    && !seCruza(visitasCliente, inicio, fin, idVisitaIgnorada)
                    && !seCruza(visitasPropiedad, inicio, fin, idVisitaIgnorada)) {
                libres.add(inicio.format(HORA_FMT));
            }
        }
        return libres;
    }

    // =========================================================================
    // MÉTODOS PRIVADOS EXTRAÍDOS SEGÚN EL DIAGRAMA DE VisitaServiceImpl (Punto 16)
    // =========================================================================

    private Propiedad validarDisponibilidad(Long idPropiedad) {
        Propiedad propiedad = propiedadRepository.findById(idPropiedad)
                .orElseThrow(() -> new AppException("La propiedad no existe.", 404));

        if (propiedad.getEstado() != EstadoPropiedad.DISPONIBLE) {
            throw new AppException("RN-01: Solo una propiedad disponible puede recibir visitas.", 400);
        }
        return propiedad;
    }

    private LocalTime calcularHoraFin(LocalTime horaInicio) {
        if (horaInicio.getHour() > 22) {
            throw new AppException("Hora de inicio fuera de rango permitido.", 400);
        }
        return horaInicio.plusMinutes(60);
    }

    private void validarCruceHorario(Long idAsesor, Long idCliente, Long idPropiedad,
                                     LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                     Long idVisitaIgnorada) {
        // 1. Cruce en la agenda del asesor (RN-04)
        List<Visita> visitasAsesor = visitaRepository.findByUsuarioIdAndFecha(idAsesor, fecha);
        if (seCruza(visitasAsesor, horaInicio, horaFin, idVisitaIgnorada)) {
            throw new AppException("RN-04: El asesor ya tiene una visita programada que se cruza en ese horario.", 400);
        }

        // 2. Cruce en la agenda del cliente
        List<Visita> visitasCliente = visitaRepository.findByClienteIdAndFecha(idCliente, fecha);
        if (seCruza(visitasCliente, horaInicio, horaFin, idVisitaIgnorada)) {
            throw new AppException("RN-04: El cliente ya tiene otra visita programada en ese horario (no puede estar en dos propiedades a la misma hora).", 400);
        }

        // 3. Cruce o duplicado en la misma propiedad
        List<Visita> visitasPropiedad = visitaRepository.findByPropiedadIdAndFecha(idPropiedad, fecha);
        boolean mismaPropiedadMismoCliente = visitasPropiedad.stream()
                .filter(v -> !Objects.equals(v.getId(), idVisitaIgnorada))
                .anyMatch(v -> v.getEstado() != EstadoVisita.CANCELADA && Objects.equals(v.getIdCliente(), idCliente));
        if (mismaPropiedadMismoCliente) {
            throw new AppException("Este cliente ya tiene una visita pendiente en esta misma propiedad para esa fecha.", 400);
        }
        if (seCruza(visitasPropiedad, horaInicio, horaFin, idVisitaIgnorada)) {
            throw new AppException("La propiedad ya tiene otra visita programada que se cruza en ese horario.", 400);
        }
    }

    // Punto 12: validarAsesor verifica que quien atiende la visita tenga rol ASESOR usando UsuarioRepository
    private Usuario validarAsesor(Long idAsesorDto, Usuario usuarioSesion) {
        if (usuarioSesion.getRol() == Rol.ASESOR) {
            return usuarioRepository.findById(usuarioSesion.getId())
                    .orElseThrow(() -> new AppException("Asesor en sesión no encontrado.", 404));
        }

        // Si agenda el ADMINISTRADOR, debe indicar idAsesor válido con rol ASESOR
        if (idAsesorDto != null) {
            Usuario asesor = usuarioRepository.findById(idAsesorDto)
                    .orElseThrow(() -> new AppException("El asesor seleccionado no existe.", 404));
            if (asesor.getRol() != Rol.ASESOR) {
                throw new AppException("RN-07: El usuario asignado a la visita debe tener rol de ASESOR.", 400);
            }
            return asesor;
        }

        // Fallback compatible si no se envía idAsesor explícito desde un cliente externo:
        // asignar el primer asesor registrado o exigir selección si no hay asesores
        return usuarioRepository.findByRolOrderByNombresAsc(Rol.ASESOR).stream()
                .findFirst()
                .orElseThrow(() -> new AppException("Debe seleccionar un asesor válido para atender la visita.", 400));
    }

    private HistorialVisita registrarHistorial(Visita visita, Usuario usuarioResponsable,
                                               TipoCambio tipoCambio, String motivo,
                                               String fechaAnterior, String fechaNueva) {
        HistorialVisita historial = new HistorialVisita(
                visita,
                usuarioResponsable,
                tipoCambio,
                motivo,
                LocalDateTime.now(),
                fechaAnterior,
                fechaNueva
        );
        return historialVisitaRepository.save(historial);
    }

    private boolean seCruza(List<Visita> visitas, LocalTime horaInicio, LocalTime horaFin, Long idVisitaIgnorada) {
        return visitas.stream()
                .filter(v -> !Objects.equals(v.getId(), idVisitaIgnorada))
                .filter(v -> v.getEstado() != EstadoVisita.CANCELADA)
                .anyMatch(v -> horaInicio.isBefore(v.getHoraFin()) && horaFin.isAfter(v.getHoraInicio()));
    }

    private void validarHorarioAtencion(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        if (fecha.isBefore(hoy) || (fecha.isEqual(hoy) && !horaInicio.isAfter(ahora))) {
            throw new AppException("Horario de atención: No se pueden agendar visitas en fechas u horas pasadas.", 400);
        }

        DayOfWeek dia = fecha.getDayOfWeek();
        if (dia == DayOfWeek.SUNDAY) {
            throw new AppException("Horario de atención: La inmobiliaria no atiende los días domingo.", 400);
        }

        if (dia == DayOfWeek.SATURDAY) {
            if (horaInicio.isBefore(HorarioAtencion.APERTURA_SABADO) || horaFin.isAfter(HorarioAtencion.CIERRE_SABADO)) {
                throw new AppException("Horario de atención los sábados: 09:00 a 13:00 (última cita inicia a las 12:00).", 400);
            }
            return;
        }

        if (horaInicio.isBefore(HorarioAtencion.APERTURA_LV) || horaFin.isAfter(HorarioAtencion.CIERRE_LV)) {
            throw new AppException("Horario de atención de lunes a viernes: 09:00 a 18:00 (última cita inicia a las 17:00).", 400);
        }
    }

    private String formatearFechaHora(LocalDate fecha, LocalTime hora) {
        return fecha.toString() + " " + hora.format(HORA_FMT);
    }

    private Long parseLong(Object val) {
        if (val == null || String.valueOf(val).isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(val).trim());
        } catch (NumberFormatException e) {
            throw new AppException("Identificador numérico inválido.", 400);
        }
    }

    public static LocalDate parseFecha(String fechaStr) {
        try {
            return LocalDate.parse(fechaStr);
        } catch (DateTimeParseException e) {
            throw new AppException("Formato de fecha inválido. Use YYYY-MM-DD.", 400);
        }
    }

    public static LocalTime parseHora(String horaStr) {
        try {
            return LocalTime.parse(horaStr, HORA_FMT);
        } catch (DateTimeParseException e) {
            throw new AppException("Formato de hora inválido. Use HH:MM (ej. 10:00).", 400);
        }
    }
}
