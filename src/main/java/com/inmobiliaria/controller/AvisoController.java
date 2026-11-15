package com.inmobiliaria.controller;

import com.inmobiliaria.config.AuthInterceptor;
import com.inmobiliaria.model.Aviso;
import com.inmobiliaria.model.Usuario;
import com.inmobiliaria.service.AvisoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Segunda Familia: Controller - AvisoController (Punto 14 / HU-11 / RF-13).
 */
@RestController
@RequestMapping("/api/avisos")
public class AvisoController {

    private final AvisoService avisoService;

    public AvisoController(AvisoService avisoService) {
        this.avisoService = avisoService;
    }

    @GetMapping
    public ResponseEntity<List<Aviso>> listarAvisos(HttpServletRequest request) {
        Usuario usuarioSesion = (Usuario) request.getAttribute(AuthInterceptor.REQUEST_USER_ATTR);
        return ResponseEntity.ok(avisoService.listarAvisos(usuarioSesion));
    }
}
