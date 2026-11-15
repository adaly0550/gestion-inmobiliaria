package com.inmobiliaria.service.impl;

import com.inmobiliaria.constants.EstadoPropiedad;
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

        String codigoLimpio = propiedad.getCodigoInterno().trim();
        if (propiedadRepository.findByCodigoInterno(codigoLimpio).isPresent()) {
            throw new AppException("Ya existe una propiedad con el código interno " + codigoLimpio + ".", 400);
        }

        propiedad.setCodigoInterno(codigoLimpio);
        propiedad.setDireccion(propiedad.getDireccion().trim());
        propiedad.setTipo(propiedad.getTipo().trim());
        propiedad.setDescripcion(propiedad.getDescripcion() != null ? propiedad.getDescripcion().trim() : null);
        if (propiedad.getEstado() == null) {
            propiedad.setEstado(EstadoPropiedad.DISPONIBLE);
        }

        return propiedadRepository.save(propiedad);
    }

    @Override
    @Transactional
    public Propiedad cambiarEstado(Long idPropiedad, String nuevoEstadoStr, Usuario usuarioSesion) {
        validarRol(usuarioSesion);

        if (nuevoEstadoStr == null || nuevoEstadoStr.isBlank()) {
            throw new AppException("Debe indicar el nuevo estado de la propiedad.", 400);
        }

        EstadoPropiedad nuevoEstado;
        try {
            nuevoEstado = EstadoPropiedad.valueOf(nuevoEstadoStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Estado no válido. Valores permitidos: " + Arrays.toString(EstadoPropiedad.values()), 400);
        }

        Propiedad propiedad = propiedadRepository.findById(idPropiedad)
                .orElseThrow(() -> new AppException("Propiedad no encontrada.", 404));

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
                || p.getCodigoInterno() == null || p.getCodigoInterno().isBlank()
                || p.getDireccion() == null || p.getDireccion().isBlank()
                || p.getTipo() == null || p.getTipo().isBlank()
                || p.getModalidad() == null) {
            throw new AppException("RN-05: Todos los datos básicos son obligatorios (codigoInterno, direccion, tipo, modalidad).", 400);
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
