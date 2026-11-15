package com.inmobiliaria.service;

import com.inmobiliaria.model.HistorialVisita;
import com.inmobiliaria.model.Usuario;

import java.util.List;

/**
 * Tercera Familia: Service (Interface) - AuditoriaService (Punto 14).
 */
public interface AuditoriaService {

    List<HistorialVisita> listarTodoElHistorial(Usuario usuarioSesion);
}
