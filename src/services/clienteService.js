// src/services/clienteService.js
import clienteRepository from '../repositories/clienteRepository.js';

// 1. Servicio para listar todos los clientes
export const listarClientes = async () => {
  return await clienteRepository.findAll();
};

// 2. Servicio para buscar un cliente por su ID
export const obtenerClientePorId = async (id) => {
  const cliente = await clienteRepository.findById(id);
  if (!cliente) {
    throw new Error('Cliente no encontrado.');
  }
  return cliente;
};

// 3. Servicio para crear un nuevo cliente evitando duplicados por identificación
export const crearCliente = async (datos) => {
  const { identificacion, nombre, nombres, telefono } = datos;
  const nombreFinal = nombre || nombres;

  // Regla de negocio RN-05: campos obligatorios
  if (!identificacion || !nombreFinal || !telefono) {
    throw new Error('RN-05: Identificación, nombre y teléfono son obligatorios.');
  }

  const identificacionLimpia = String(identificacion).trim();

  // Validar que no exista otro cliente con la misma identificación
  const clienteExistente = await clienteRepository.findByIdentificacion(identificacionLimpia);
  if (clienteExistente) {
    throw new Error(`Ya existe un cliente registrado con la identificación ${identificacionLimpia}.`);
  }

  return await clienteRepository.save({
    ...datos,
    identificacion: identificacionLimpia
  });
};

export default {
  listarClientes,
  obtenerClientePorId,
  crearCliente
};
