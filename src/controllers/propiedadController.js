// src/controllers/propiedadController.js
import propiedadService from '../services/propiedadService.js';

class PropiedadController {
  async listarDisponibles(req, res) {
    try {
      const propiedades = await propiedadService.listarDisponibles();
      return res.status(200).json(propiedades);
    } catch (error) {
      return res.status(400).json({ error: error.message });
    }
  }

  async listarTodas(req, res) {
    try {
      const propiedades = await propiedadService.listarTodas();
      return res.status(200).json(propiedades);
    } catch (error) {
      return res.status(400).json({ error: error.message });
    }
  }

  async registrar(req, res) {
    try {
      const propiedad = await propiedadService.registrar(req.body);
      return res.status(201).json(propiedad);
    } catch (error) {
      return res.status(400).json({ error: error.message });
    }
  }

  async cambiarEstado(req, res) {
    try {
      const { id } = req.params;
      const { estado } = req.body;
      const propiedad = await propiedadService.cambiarEstado(Number(id), estado, req.usuario);
      return res.status(200).json(propiedad);
    } catch (error) {
      return res.status(400).json({ error: error.message });
    }
  }
}

export default new PropiedadController();
