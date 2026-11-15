package com.inmobiliaria.controller;

import com.inmobiliaria.config.AuthInterceptor;
import com.inmobiliaria.constants.EstadoPropiedad;
import com.inmobiliaria.constants.Modalidad;
import com.inmobiliaria.exception.AppException;
import com.inmobiliaria.model.Propiedad;
import com.inmobiliaria.model.Usuario;
import com.inmobiliaria.service.PropiedadService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Segunda Familia: Controller - PropiedadesController.
 * Depende de la interface PropiedadService (Punto 18) y atiende tanto /api/propiedades como /propiedades (Punto 25).
 */
@RestController
@RequestMapping({"/api/propiedades", "/propiedades"})
public class PropiedadesController {

    private final PropiedadService propiedadService;

    public PropiedadesController(PropiedadService propiedadService) {
        this.propiedadService = propiedadService;
    }

    // POST /propiedades (HU-01, RF-01)
    @PostMapping
    public ResponseEntity<Propiedad> registrarPropiedad(@RequestBody Map<String, Object> dto,
                                                        HttpServletRequest request) {
        Usuario usuarioSesion = (Usuario) request.getAttribute(AuthInterceptor.REQUEST_USER_ATTR);

        Propiedad nueva = new Propiedad();
        nueva.setCodigoInterno(dto.get("codigoInterno") != null ? String.valueOf(dto.get("codigoInterno")) : null);
        nueva.setDireccion(dto.get("direccion") != null ? String.valueOf(dto.get("direccion")) : null);
        nueva.setTipo(dto.get("tipo") != null ? String.valueOf(dto.get("tipo")) : null);
        nueva.setDescripcion(dto.get("descripcion") != null ? String.valueOf(dto.get("descripcion")) : null);

        if (dto.get("modalidad") != null && !String.valueOf(dto.get("modalidad")).isBlank()) {
            try {
                nueva.setModalidad(Modalidad.valueOf(String.valueOf(dto.get("modalidad")).trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new AppException("RN-05: La modalidad debe ser 'VENTA' o 'ALQUILER'.", 400);
            }
        }

        if (dto.get("estado") != null && !String.valueOf(dto.get("estado")).isBlank()) {
            try {
                nueva.setEstado(EstadoPropiedad.valueOf(String.valueOf(dto.get("estado")).trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new AppException("Estado de propiedad no válido.", 400);
            }
        } else {
            nueva.setEstado(EstadoPropiedad.DISPONIBLE);
        }

        Propiedad guardada = propiedadService.registrarPropiedad(nueva, usuarioSesion);
        return ResponseEntity.status(201).body(guardada);
    }

    // PATCH /propiedades/{id}/estado (HU-02, RF-02)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Propiedad> cambiarEstado(@PathVariable Long id,
                                                   @RequestBody Map<String, String> body,
                                                   HttpServletRequest request) {
        Usuario usuarioSesion = (Usuario) request.getAttribute(AuthInterceptor.REQUEST_USER_ATTR);
        String estado = body.get("estado");
        Propiedad actualizada = propiedadService.cambiarEstado(id, estado, usuarioSesion);
        return ResponseEntity.ok(actualizada);
    }

    // GET /propiedades?estado=DISPONIBLE (HU-03, RF-03, Punto 25) y listado general
    @GetMapping
    public ResponseEntity<List<Propiedad>> listar(@RequestParam(required = false) String estado) {
        if (estado != null && estado.equalsIgnoreCase(EstadoPropiedad.DISPONIBLE.name())) {
            return ResponseEntity.ok(propiedadService.listarDisponibles());
        }
        return ResponseEntity.ok(propiedadService.listarTodas());
    }

    // Alias GET /propiedades/disponibles (HU-03, RF-03)
    @GetMapping("/disponibles")
    public ResponseEntity<List<Propiedad>> listarDisponibles() {
        return ResponseEntity.ok(propiedadService.listarDisponibles());
    }
}
