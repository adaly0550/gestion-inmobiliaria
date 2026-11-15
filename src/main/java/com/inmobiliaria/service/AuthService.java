package com.inmobiliaria.service;

import com.inmobiliaria.model.Usuario;

import java.util.List;

/**
 * Tercera Familia: Service (Interface) - AuthService (Puntos 8, 9, 14, 24).
 */
public interface AuthService {

    Usuario login(String usuario, String password);

    Usuario obtenerPorId(Long id);

    List<Usuario> listarAsesores();

    Usuario registrarAsesor(String nombres, String usuario, String password, Usuario usuarioSesion);
}
