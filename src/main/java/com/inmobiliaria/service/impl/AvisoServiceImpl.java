package com.inmobiliaria.service.impl;

import com.inmobiliaria.constants.Rol;
import com.inmobiliaria.model.Aviso;
import com.inmobiliaria.model.Usuario;
import com.inmobiliaria.repository.AvisoRepository;
import com.inmobiliaria.service.AvisoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cuarta Familia: Implementación - AvisoServiceImpl (Punto 14 / RN-10).
 * El Administrador consulta todos los avisos; el Asesor solo los suyos.
 */
@Service
public class AvisoServiceImpl implements AvisoService {

    private final AvisoRepository avisoRepository;

    public AvisoServiceImpl(AvisoRepository avisoRepository) {
        this.avisoRepository = avisoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Aviso> listarAvisos(Usuario usuarioSesion) {
        if (usuarioSesion.getRol() == Rol.ADMINISTRADOR) {
            return avisoRepository.findAllByOrderByIdDesc();
        }
        return avisoRepository.findByUsuarioIdOrderByIdDesc(usuarioSesion.getId());
    }
}
