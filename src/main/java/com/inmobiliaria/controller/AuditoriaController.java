package com.inmobiliaria.controller;

import com.inmobiliaria.config.AuthInterceptor;
import com.inmobiliaria.model.HistorialVisita;
import com.inmobiliaria.model.Usuario;
import com.inmobiliaria.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Segunda Familia: Controller - AuditoriaController (Punto 14).
 */
@RestController
@RequestMapping("/api/auditor")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<List<HistorialVisita>> listarAuditoria(HttpServletRequest request) {
        Usuario usuarioSesion = (Usuario) request.getAttribute(AuthInterceptor.REQUEST_USER_ATTR);
        return ResponseEntity.ok(auditoriaService.listarTodoElHistorial(usuarioSesion));
    }
}
