package com.inmobiliaria.service;

import com.inmobiliaria.model.Propiedad;
import com.inmobiliaria.model.Usuario;

import java.util.List;

/**
 * Tercera Familia: Service (Interface) - PropiedadService.
 * Define el contrato de las operaciones del módulo Propiedades (HU-01, HU-02, HU-03).
 */
public interface PropiedadService {

    Propiedad registrarPropiedad(Propiedad propiedad, Usuario usuarioSesion);

    Propiedad cambiarEstado(Long idPropiedad, String nuevoEstado, Usuario usuarioSesion);

    List<Propiedad> listarDisponibles();

    List<Propiedad> listarTodas();
}
