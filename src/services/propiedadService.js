// src/services/propiedadService.js
import propiedadRepository from '../repositories/propiedadRepository.js';
import visitaService from './visitaService.js';
import { EstadoPropiedad } from '../constants/enums.js';

class PropiedadService {
  async listarDisponibles() {
    return await propiedadRepository.findDisponibles();
  }

  async listarTodas() {
    return await propiedadRepository.findAll();
  }

  async registrar({ codigoInterno, direccion, tipo, modalidad, estado = EstadoPropiedad.DISPONIBLE }) {
    if (!codigoInterno || !direccion || !tipo || !modalidad) {
      throw new Error('Todos los campos son obligatorios: codigoInterno, direccion, tipo, modalidad.');
    }

    const existente = await propiedadRepository.findByCodigo(codigoInterno.trim());
    if (existente) {
      throw new Error(`Ya existe una propiedad con el código interno ${codigoInterno}.`);
    }

    const nuevaPropiedad = await propiedadRepository.save({
      codigoInterno: codigoInterno.trim(),
      direccion: direccion.trim(),
      tipo: tipo.trim(),
      modalidad: modalidad.trim(),
      estado
    });

    return nuevaPropiedad;
  }

  async cambiarEstado(id, nuevoEstado, usuario) {
    if (!Object.values(EstadoPropiedad).includes(nuevoEstado)) {
      throw new Error(`Estado no válido. Valores permitidos: ${Object.values(EstadoPropiedad).join(', ')}`);
    }

    const propiedad = await propiedadRepository.findById(id);
    if (!propiedad) {
      throw new Error('Propiedad no encontrada.');
    }

    const propiedadActualizada = await propiedadRepository.updateEstado(id, nuevoEstado);

    // RN-02: Cancelación automática de visitas si deja de estar DISPONIBLE
    if (nuevoEstado !== EstadoPropiedad.DISPONIBLE) {
      await visitaService.cancelarVisitasPorPropiedad(
        id,
        `Cancelación automática: Propiedad ${propiedad.codigoInterno} cambió a estado ${nuevoEstado}`,
        usuario
      );
    }

    return propiedadActualizada;
  }
}

export default new PropiedadService();
