// src/controllers/visitaController.js
import visitaService from '../services/visitaService.js';

class VisitaController {
  // POST /api/visitas
  async agendar(req, res) {
    try {
      const data = req.body;
      const usuarioSesion = req.usuario;
      const nuevaVisita = await visitaService.agendarVisita(data, usuarioSesion);
      return res.status(201).json(nuevaVisita);
    } catch (error) {
      return res.status(400).json({ error: error.message });
    }
  }

  // PATCH /api/visitas/:id/cancelar
  async cancelar(req, res) {
    try {
      const { id } = req.params;
      const { motivo } = req.body;
      const usuarioSesion = req.usuario;
      const resultado = await visitaService.cancelarVisita(Number(id), motivo, usuarioSesion);
      return res.status(200).json(resultado);
    } catch (error) {
      return res.status(400).json({ error: error.message });
    }
  }

  // GET /api/visitas/pendientes
  async listarPendientes(req, res) {
    try {
      const { fecha } = req.query;
      const usuarioSesion = req.usuario;
      const visitas = await visitaService.listarPendientes(fecha, usuarioSesion);
      return res.status(200).json(visitas);
    } catch (error) {
      return res.status(400).json({ error: error.message });
    }
  }

  // GET /api/visitas
  async listarTodas(req, res) {
    try {
      const { fecha } = req.query;
      const usuarioSesion = req.usuario;
      const visitas = await visitaService.listarTodas(fecha, usuarioSesion);
      return res.status(200).json(visitas);
    } catch (error) {
      return res.status(400).json({ error: error.message });
    }
  }
}

export default new VisitaController();
