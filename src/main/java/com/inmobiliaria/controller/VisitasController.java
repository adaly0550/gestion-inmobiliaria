package com.inmobiliaria.controller;

import com.inmobiliaria.config.AuthInterceptor;
import com.inmobiliaria.exception.AppException;
import com.inmobiliaria.model.HistorialVisita;
import com.inmobiliaria.model.Usuario;
import com.inmobiliaria.model.Visita;
import com.inmobiliaria.service.VisitaService;
import com.inmobiliaria.service.impl.VisitaServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Segunda Familia: Controller - VisitasController.
 * Incluye las 6 operaciones de su diagrama del PDF más la consulta de horarios libres (Punto 31).
 */
@RestController
@RequestMapping({"/api/visitas", "/visitas"})
public class VisitasController {

    private final VisitaService visitaService;

    public VisitasController(VisitaService visitaService) {
        this.visitaService = visitaService;
    }

    // 1. POST /visitas (HU-05, RF-05, RF-06, RF-07)
    @PostMapping
    public ResponseEntity<Visita> agendarVisita(@RequestBody Map<String, Object> dto,
                                                HttpServletRequest request) {
        Usuario usuarioSesion = (Usuario) request.getAttribute(AuthInterceptor.REQUEST_USER_ATTR);
        Visita creada = visitaService.agendarVisita(dto, usuarioSesion);
        return ResponseEntity.status(201).body(creada);
    }

    // 2. PATCH /visitas/{id}/cancelar (HU-06, RF-08)
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Visita> cancelarVisita(@PathVariable Long id,
                                                 @RequestBody Map<String, String> body,
                                                 HttpServletRequest request) {
        Usuario usuarioSesion = (Usuario) request.getAttribute(AuthInterceptor.REQUEST_USER_ATTR);
        String motivo = body.get("motivo");
        Visita cancelada = visitaService.cancelarVisita(id, motivo, usuarioSesion);
        return ResponseEntity.ok(cancelada);
    }

    // 3. PATCH /visitas/{id}/reprogramar (Punto 1 / HU-06 / RF-08)
    @PatchMapping("/{id}/reprogramar")
    public ResponseEntity<Visita> reprogramarVisita(@PathVariable Long id,
                                                    @RequestBody Map<String, String> dto,
                                                    HttpServletRequest request) {
        Usuario usuarioSesion = (Usuario) request.getAttribute(AuthInterceptor.REQUEST_USER_ATTR);

        String fechaStr = dto.get("fecha") != null ? dto.get("fecha") : dto.get("nuevaFecha");
        String horaStr = dto.get("horaInicio") != null ? dto.get("horaInicio") : dto.get("nuevaHora");
        String motivo = dto.get("motivo");

        if (motivo == null || motivo.isBlank()) {
            throw new AppException("RN-08: Se requiere un motivo obligatorio para reprogramar la visita.", 400);
        }
        if (fechaStr == null || fechaStr.isBlank() || horaStr == null || horaStr.isBlank()) {
            throw new AppException("RN-03: Debe indicar la nueva fecha y la nueva hora de inicio para reprogramar.", 400);
        }

        LocalDate nuevaFecha = VisitaServiceImpl.parseFecha(fechaStr.trim());
        LocalTime nuevaHora = VisitaServiceImpl.parseHora(horaStr.trim());

        Visita reprogramada = visitaService.reprogramarVisita(id, nuevaFecha, nuevaHora, motivo, usuarioSesion);
        return ResponseEntity.ok(reprogramada);
    }

    // 4. GET /visitas/pendientes?fecha=...&asesor=... (Punto 5 / HU-08 / RF-10)
    @GetMapping("/pendientes")
    public ResponseEntity<List<Visita>> listarPendientes(@RequestParam(required = false) String fecha,
                                                         @RequestParam(required = false) Long asesor,
                                                         HttpServletRequest request) {
        Usuario usuarioSesion = (Usuario) request.getAttribute(AuthInterceptor.REQUEST_USER_ATTR);
        LocalDate fechaFiltro = (fecha != null && !fecha.isBlank()) ? VisitaServiceImpl.parseFecha(fecha.trim()) : null;
        return ResponseEntity.ok(visitaService.listarPendientes(fechaFiltro, asesor, usuarioSesion));
    }

    // 5. GET /visitas/{id}/historial (Punto 2 / HU-09 / RF-11)
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialVisita>> consultarHistorial(@PathVariable Long id,
                                                                    HttpServletRequest request) {
        Usuario usuarioSesion = (Usuario) request.getAttribute(AuthInterceptor.REQUEST_USER_ATTR);
        return ResponseEntity.ok(visitaService.consultarHistorial(id, usuarioSesion));
    }

    // 6. GET /visitas?cliente=... o ?propiedad=... o ?fecha=... (Punto 3 / HU-10 / RF-12)
    @GetMapping
    public ResponseEntity<List<Visita>> listarPorClienteOPropiedad(@RequestParam(required = false) Long cliente,
                                                                   @RequestParam(required = false) Long propiedad,
                                                                   @RequestParam(required = false) String fecha,
                                                                   HttpServletRequest request) {
        Usuario usuarioSesion = (Usuario) request.getAttribute(AuthInterceptor.REQUEST_USER_ATTR);

        if (cliente != null || propiedad != null) {
            return ResponseEntity.ok(visitaService.listarPorClienteOPropiedad(cliente, propiedad, usuarioSesion));
        }

        LocalDate fechaFiltro = (fecha != null && !fecha.isBlank()) ? VisitaServiceImpl.parseFecha(fecha.trim()) : null;
        return ResponseEntity.ok(visitaService.listarTodas(fechaFiltro, usuarioSesion));
    }

    // 7. GET /visitas/horarios-libres (Punto 31 / HU-05)
    @GetMapping("/horarios-libres")
    public ResponseEntity<List<String>> obtenerHorariosLibres(@RequestParam String fecha,
                                                              @RequestParam(required = false) Long propiedad,
                                                              @RequestParam(required = false) Long cliente,
                                                              @RequestParam(required = false) Long asesor,
                                                              @RequestParam(required = false) Long visitaIgnorada,
                                                              HttpServletRequest request) {
        Usuario usuarioSesion = (Usuario) request.getAttribute(AuthInterceptor.REQUEST_USER_ATTR);
        LocalDate fechaParsed = VisitaServiceImpl.parseFecha(fecha.trim());
        return ResponseEntity.ok(visitaService.obtenerHorariosLibres(
                fechaParsed, propiedad, cliente, asesor, visitaIgnorada, usuarioSesion
        ));
    }
}
