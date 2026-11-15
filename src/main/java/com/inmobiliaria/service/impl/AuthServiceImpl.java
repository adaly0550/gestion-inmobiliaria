package com.inmobiliaria.service.impl;

import com.inmobiliaria.constants.Rol;
import com.inmobiliaria.exception.AppException;
import com.inmobiliaria.model.Usuario;
import com.inmobiliaria.repository.UsuarioRepository;
import com.inmobiliaria.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cuarta Familia: Implementación - AuthServiceImpl (Puntos 8, 9, 14 y 24).
 * Compara contraseñas con BCrypt y permite el registro de asesores por el Administrador.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario login(String usuario, String password) {
        if (usuario == null || usuario.isBlank() || password == null || password.isBlank()) {
            throw new AppException("Debe ingresar usuario y contraseña.", 400);
        }

        Usuario encontrado = usuarioRepository.findByUsuario(usuario.trim())
                .orElseThrow(() -> new AppException("Usuario o contraseña incorrectos.", 401));

        // Punto 9: Comparación segura mediante BCrypt
        if (!passwordEncoder.matches(password, encontrado.getContrasena())) {
            throw new AppException("Usuario o contraseña incorrectos.", 401);
        }

        return encontrado;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new AppException("Usuario no encontrado.", 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarAsesores() {
        return usuarioRepository.findByRolOrderByNombresAsc(Rol.ASESOR);
    }

    // Punto 24: Registro de nuevos asesores (solo por el Administrador, con contraseña cifrada en BCrypt)
    @Override
    @Transactional
    public Usuario registrarAsesor(String nombres, String usuario, String password, Usuario usuarioSesion) {
        if (usuarioSesion == null || usuarioSesion.getRol() != Rol.ADMINISTRADOR) {
            throw new AppException("RN-06: Solo el administrador puede registrar asesores.", 403);
        }

        if (nombres == null || nombres.isBlank() || usuario == null || usuario.isBlank() || password == null || password.isBlank()) {
            throw new AppException("Nombres, usuario y contraseña son obligatorios para registrar un asesor.", 400);
        }

        String usuarioLimpio = usuario.trim();
        if (usuarioRepository.findByUsuario(usuarioLimpio).isPresent()) {
            throw new AppException("Ya existe un usuario registrado con el nombre de usuario '" + usuarioLimpio + "'.", 400);
        }

        Usuario nuevo = new Usuario(
                null,
                nombres.trim(),
                usuarioLimpio,
                passwordEncoder.encode(password),
                Rol.ASESOR
        );

        return usuarioRepository.save(nuevo);
    }
}
