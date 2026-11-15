// src/repositories/clienteRepository.js
import { conectarDB } from '../config/database.js';

export const obtenerTodosLosClientes = async () => {
  const db = await conectarDB();
  return await db.all('SELECT * FROM Clientes ORDER BY id DESC');
};

export const obtenerClientePorId = async (id) => {
  const db = await conectarDB();
  const cliente = await db.get('SELECT * FROM Clientes WHERE id = ?', [Number(id)]);
  return cliente || null;
};

export const obtenerClientePorIdentificacion = async (identificacion) => {
  const db = await conectarDB();
  const cliente = await db.get('SELECT * FROM Clientes WHERE identificacion = ?', [identificacion]);
  return cliente || null;
};

export const crearCliente = async (datos) => {
  const db = await conectarDB();
  const { identificacion, nombre, apellido, nombres, telefono, correo } = datos;

  const nombreFinal = nombre || nombres || '';
  const apellidoFinal = apellido || '';
  const correoFinal = correo || null;

  const resultado = await db.run(
    'INSERT INTO Clientes (identificacion, nombre, apellido, telefono, correo) VALUES (?, ?, ?, ?, ?)',
    [identificacion, nombreFinal, apellidoFinal, telefono, correoFinal]
  );

  return await obtenerClientePorId(resultado.lastID);
};

class ClienteRepository {
  async findAll() {
    return await obtenerTodosLosClientes();
  }

  async findById(id) {
    return await obtenerClientePorId(id);
  }

  async findByIdentificacion(identificacion) {
    return await obtenerClientePorIdentificacion(identificacion);
  }

  async save(datos) {
    return await crearCliente(datos);
  }
}

export default new ClienteRepository();
