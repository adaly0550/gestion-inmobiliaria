// src/services/clienteService.js
import * as clienteRepository from '../repositories/clienteRepository.js';

// 1. Servicio para listar todos los clientes
export const listarClientes = async () => {
  try {
    const clientes = await clienteRepository.obtenerTodosLosClientes();
    return clientes;
  } catch (error) {
    console.error('Error al listar clientes en el servicio:', error.message);
    throw error;
  }
};

// 2. Servicio para buscar un cliente por su ID
export const obtenerClientePorId = async (id) => {
  try {
    const cliente = await clienteRepository.obtenerClientePorId(id);
    if (!cliente) {
      throw new Error('Cliente no encontrado.');
    }
    return cliente;
  } catch (error) {
    console.error(`Error al obtener el cliente con ID ${id}:`, error.message);
    throw error;
  }
};

// 3. Servicio para crear un nuevo cliente evitando duplicados por identificación
export const crearCliente = async (datos) => {
  try {
    const { identificacion, nombre, nombres, telefono } = datos;
    const nombreFinal = nombre || nombres;

    // Regla de negocio RN-05: campos obligatorios
    if (!identificacion || !nombreFinal || !telefono) {
      throw new Error('RN-05: Identificación, nombre y teléfono son obligatorios.');
    }

    const identificacionLimpia = String(identificacion).trim();

    // Validar que no exista otro cliente con la misma identificación
    const clienteExistente = await clienteRepository.obtenerClientePorIdentificacion(identificacionLimpia);
    if (clienteExistente) {
      throw new Error(`Ya existe un cliente registrado con la identificación ${identificacionLimpia}.`);
    }

    const clienteCreado = await clienteRepository.crearCliente({
      ...datos,
      identificacion: identificacionLimpia
    });
    return clienteCreado;
  } catch (error) {
    console.error('Error al crear cliente en el servicio:', error.message);
    throw error;
  }
};

export const registrarCliente = crearCliente;

export default {
  listarClientes,
  obtenerClientePorId,
  crearCliente,
  registrarCliente
};
