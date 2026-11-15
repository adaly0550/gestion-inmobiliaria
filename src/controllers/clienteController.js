// src/controllers/clienteController.js
import clienteService from '../services/clienteService.js';

// 1. Obtener todos los clientes (GET /api/clientes)
export const obtenerClientes = async (req, res) => {
  try {
    const clientes = await clienteService.listarClientes();
    return res.status(200).json(clientes);
  } catch (error) {
    return res.status(500).json({ error: error.message });
  }
};

// 2. Obtener un cliente por su ID (GET /api/clientes/:id)
export const obtenerClientePorId = async (req, res) => {
  try {
    const { id } = req.params;
    const cliente = await clienteService.obtenerClientePorId(id);
    return res.status(200).json(cliente);
  } catch (error) {
    return res.status(404).json({ error: error.message });
  }
};

// 3. Crear un nuevo cliente (POST /api/clientes)
export const crearCliente = async (req, res) => {
  try {
    const nuevoCliente = await clienteService.crearCliente(req.body);
    return res.status(201).json(nuevoCliente);
  } catch (error) {
    return res.status(400).json({ error: error.message });
  }
};

export default {
  obtenerClientes,
  obtenerClientePorId,
  crearCliente
};
