package com.inmobiliaria.service.impl;

import com.inmobiliaria.constants.EstadoPropiedad;
import com.inmobiliaria.constants.Modalidad;
import com.inmobiliaria.constants.Rol;
import com.inmobiliaria.constants.TipoPropiedad;
import com.inmobiliaria.exception.AppException;
import com.inmobiliaria.model.Propiedad;
import com.inmobiliaria.model.Usuario;
import com.inmobiliaria.repository.PropiedadRepository;
import com.inmobiliaria.service.PropiedadService;
import com.inmobiliaria.service.VisitaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Cuarta Familia: Implementación - PropiedadServiceImpl.
 * Cumple el contrato PropiedadService e incluye los métodos privados validarDatos() y validarRol()
 * especificados en el diagrama de arquitectura por capas del PDF.
 */
@Service
public class PropiedadServiceImpl implements PropiedadService {

    private final PropiedadRepository propiedadRepository;
    private final VisitaService visitaService;

    public PropiedadServiceImpl(PropiedadRepository propiedadRepository, VisitaService visitaService) {
        this.propiedadRepository = propiedadRepository;
        this.visitaService = visitaService;
    }

    @Override
    @Transactional
    public Propiedad registrarPropiedad(Propiedad propiedad, Usuario usuarioSesion) {
        validarRol(usuarioSesion);
        validarDatos(propiedad);

        String codigoFinal;
        if (propiedad.getCodigoInterno() != null && !propiedad.getCodigoInterno().isBlank()) {
            codigoFinal = propiedad.getCodigoInterno().trim();
            if (propiedadRepository.findByCodigoInterno(codigoFinal).isPresent()) {
                throw new AppException("Ya existe una propiedad con el código interno " + codigoFinal + ".", 400);
            }
        } else {
            codigoFinal = generarSiguienteCodigoInterno();
        }

        propiedad.setCodigoInterno(codigoFinal);
        propiedad.setDireccion(propiedad.getDireccion().trim());
        propiedad.setTipo(propiedad.getTipo().trim());
        propiedad.setDescripcion(propiedad.getDescripcion() != null ? propiedad.getDescripcion().trim() : null);
        if (propiedad.getEstado() == null) {
            propiedad.setEstado(EstadoPropiedad.DISPONIBLE);
        }

        validarReglasEstado(propiedad.getModalidad(), null, propiedad.getEstado(), false);

        return propiedadRepository.save(propiedad);
    }

    // Genera automáticamente el siguiente código interno correlativo (ej. P-01, P-02, P-03...)
    private String generarSiguienteCodigoInterno() {
        List<Propiedad> existentes = propiedadRepository.findAll();
        int maxNumero = 0;
        for (Propiedad p : existentes) {
            if (p.getCodigoInterno() != null && p.getCodigoInterno().matches("(?i)^P-\\d+$")) {
                try {
                    int num = Integer.parseInt(p.getCodigoInterno().substring(2));
                    if (num > maxNumero) {
                        maxNumero = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        int siguiente = maxNumero + 1;
        String candidato = String.format("P-%02d", siguiente);
        while (propiedadRepository.findByCodigoInterno(candidato).isPresent()) {
            siguiente++;
            candidato = String.format("P-%02d", siguiente);
        }
        return candidato;
    }

    @Override
    @Transactional
    public Propiedad cambiarEstado(Long idPropiedad, String nuevoEstadoStr, Usuario usuarioSesion) {
        validarRol(usuarioSesion);

        if (nuevoEstadoStr == null || nuevoEstadoStr.isBlank()) {
            throw new AppException("Debe indicar el nuevo estado de la propiedad.", 400);
        }

        String estadoNorm = nuevoEstadoStr.trim().toUpperCase();
        if ("RESERVACION".equals(estadoNorm) || "EN_RESERVACION".equals(estadoNorm)) {
            estadoNorm = EstadoPropiedad.RESERVADA.name();
        }

        EstadoPropiedad nuevoEstado;
        try {
            nuevoEstado = EstadoPropiedad.valueOf(estadoNorm);
        } catch (IllegalArgumentException e) {
            throw new AppException("Estado no válido. Valores permitidos: " + Arrays.toString(EstadoPropiedad.values()), 400);
        }

        Propiedad propiedad = propiedadRepository.findById(idPropiedad)
                .orElseThrow(() -> new AppException("Propiedad no encontrada.", 404));

        validarReglasEstado(propiedad.getModalidad(), propiedad.getEstado(), nuevoEstado, true);

        propiedad.setEstado(nuevoEstado);
        Propiedad actualizada = propiedadRepository.save(propiedad);

        // RN-02 / Punto 7: Si pasa a RESERVADA, VENDIDA o ALQUILADA, cancelar sus visitas futuras pendientes
        if (nuevoEstado != EstadoPropiedad.DISPONIBLE) {
            String motivoPdf = switch (nuevoEstado) {
                case RESERVADA -> "propiedad reservada";
                case VENDIDA -> "propiedad vendida";
                case ALQUILADA -> "propiedad alquilada";
                default -> "propiedad no disponible";
            };
            visitaService.cancelarVisitasPorPropiedad(idPropiedad, motivoPdf, usuarioSesion);
        }

        return actualizada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Propiedad> listarDisponibles() {
        return propiedadRepository.findByEstado(EstadoPropiedad.DISPONIBLE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Propiedad> listarTodas() {
        return propiedadRepository.findAllByOrderByIdDesc();
    }

    // Método privado del diagrama de PropiedadServiceImpl (RN-05 y Punto 13)
    private void validarDatos(Propiedad p) {
        if (p == null
                || p.getDireccion() == null || p.getDireccion().isBlank()
                || p.getTipo() == null || p.getTipo().isBlank()
                || p.getModalidad() == null) {
            throw new AppException("RN-05: Todos los datos básicos son obligatorios (direccion, tipo, modalidad).", 400);
        }

        String tipoTrim = p.getTipo().trim();
        boolean tipoValido = Arrays.stream(TipoPropiedad.values())
                .anyMatch(t -> t.name().equalsIgnoreCase(tipoTrim));
        if (!tipoValido) {
            throw new AppException("RN-05: El tipo de propiedad debe ser 'Casa' o 'Departamento'.", 400);
        }
        // Normalizar capitalización ('Casa' o 'Departamento')
        p.setTipo(tipoTrim.substring(0, 1).toUpperCase() + tipoTrim.substring(1).toLowerCase());
    }

    // Reglas estrictas de negocio para el estado de reservación y coherencia con modalidad (RN-01, RN-02, RN-05)
    private void validarReglasEstado(Modalidad modalidad, EstadoPropiedad estadoActual,
                                     EstadoPropiedad nuevoEstado, boolean esCambioEstado) {
        if (modalidad == Modalidad.VENTA && nuevoEstado == EstadoPropiedad.ALQUILADA) {
            throw new AppException("RN-02 / RN-05: Una propiedad en modalidad VENTA no puede pasar a estado ALQUILADA (solo DISPONIBLE, RESERVADA o VENDIDA).", 400);
        }
        if (modalidad == Modalidad.ALQUILER && nuevoEstado == EstadoPropiedad.VENDIDA) {
            throw new AppException("RN-02 / RN-05: Una propiedad en modalidad ALQUILER no puede pasar a estado VENDIDA (solo DISPONIBLE, RESERVADA o ALQUILADA).", 400);
        }

        if (esCambioEstado && estadoActual != null) {
            if (estadoActual == nuevoEstado) {
                throw new AppException("RN-02: La propiedad ya se encuentra en estado " + nuevoEstado + ".", 400);
            }
            if (nuevoEstado == EstadoPropiedad.RESERVADA
                    && (estadoActual == EstadoPropiedad.VENDIDA || estadoActual == EstadoPropiedad.ALQUILADA)) {
                throw new AppException("RN-02: No se puede poner en reservación (RESERVADA) una propiedad que ya figura como " + estadoActual + ". Primero debe habilitarse como DISPONIBLE.", 400);
            }
        }
    }

    // Método privado del diagrama de PropiedadServiceImpl (RN-06)
    private void validarRol(Usuario usuarioSesion) {
        if (usuarioSesion == null) {
            throw new AppException("No autorizado: Debe iniciar sesión.", 401);
        }
        if (usuarioSesion.getRol() != Rol.ADMINISTRADOR) {
            throw new AppException("RN-06: Acceso denegado. Solo el administrador puede registrar o cambiar el estado de propiedades.", 403);
        }
    }
}
