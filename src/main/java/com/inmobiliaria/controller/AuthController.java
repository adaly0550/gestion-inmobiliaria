package com.inmobiliaria.controller;

import com.inmobiliaria.config.AuthInterceptor;
import com.inmobiliaria.model.Usuario;
import com.inmobiliaria.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Segunda Familia: Controller - AuthController (Puntos 5, 8, 9, 14 y 24).
 * Gestiona login con sesión HTTP httpOnly, logout, usuario actual y listado/registro de asesores.
 */
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<Usuario> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Usuario autenticado = authService.login(body.get("usuario"), body.get("password"));

        // Punto 8: Crear sesión en el servidor guardando únicamente el ID del usuario
        HttpSession session = request.getSession(true);
        session.setAttribute(AuthInterceptor.SESSION_USER_ID, autenticado.getId());

        return ResponseEntity.ok(autenticado);
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente."));
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<Usuario> obtenerSesionActual(HttpServletRequest request) {
        Usuario usuarioSesion = (Usuario) request.getAttribute(AuthInterceptor.REQUEST_USER_ATTR);
        return ResponseEntity.ok(usuarioSesion);
    }

    // Punto 5 y 12: Listar asesores para selectores de filtro y agendamiento por Administrador
    @GetMapping({"/api/usuarios/asesores", "/api/asesores"})
    public ResponseEntity<List<Usuario>> listarAsesores() {
        return ResponseEntity.ok(authService.listarAsesores());
    }

    // Punto 24: Registrar nuevo asesor (solo Administrador)
    @PostMapping("/api/usuarios")
    public ResponseEntity<Usuario> registrarAsesor(@RequestBody Map<String, String> body,
                                                   HttpServletRequest request) {
        Usuario usuarioSesion = (Usuario) request.getAttribute(AuthInterceptor.REQUEST_USER_ATTR);
        Usuario creado = authService.registrarAsesor(
                body.get("nombres"),
                body.get("usuario"),
                body.get("password"),
                usuarioSesion
        );
        return ResponseEntity.status(201).body(creado);
    }
}
