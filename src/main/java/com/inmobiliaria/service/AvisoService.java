package com.inmobiliaria.service;

import com.inmobiliaria.model.Aviso;
import com.inmobiliaria.model.Usuario;

import java.util.List;

/**
 * Tercera Familia: Service (Interface) - AvisoService (Punto 14).
 */
public interface AvisoService {

    List<Aviso> listarAvisos(Usuario usuarioSesion);
}
