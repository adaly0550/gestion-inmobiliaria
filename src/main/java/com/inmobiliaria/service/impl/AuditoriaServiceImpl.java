package com.inmobiliaria.service.impl;

import com.inmobiliaria.constants.Rol;
import com.inmobiliaria.exception.AppException;
import com.inmobiliaria.model.HistorialVisita;
import com.inmobiliaria.model.Usuario;
import com.inmobiliaria.repository.HistorialVisitaRepository;
import com.inmobiliaria.service.AuditoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cuarta Familia: Implementación - AuditoriaServiceImpl (Punto 14).
 */
@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    private final HistorialVisitaRepository historialVisitaRepository;

    public AuditoriaServiceImpl(HistorialVisitaRepository historialVisitaRepository) {
        this.historialVisitaRepository = historialVisitaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialVisita> listarTodoElHistorial(Usuario usuarioSesion) {
        if (usuarioSesion == null || usuarioSesion.getRol() != Rol.ADMINISTRADOR) {
            throw new AppException("Acceso denegado: Solo el administrador puede auditar.", 403);
        }
        return historialVisitaRepository.findAllByOrderByIdDesc();
    }
}
