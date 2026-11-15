// src/repositories/clienteRepository.js
import { conectarDB } from '../config/database.js';

class ClienteRepository {
  async findAll() {
    const db = await conectarDB();
    return await db.all('SELECT * FROM Clientes ORDER BY id DESC');
  }

  async findById(id) {
    const db = await conectarDB();
    return (await db.get('SELECT * FROM Clientes WHERE id = ?', [Number(id)])) || null;
  }

  async findByIdentificacion(identificacion) {
    const db = await conectarDB();
    return (await db.get('SELECT * FROM Clientes WHERE identificacion = ?', [identificacion])) || null;
  }

  async save(datos) {
    const db = await conectarDB();
    const { identificacion, nombre, apellido, nombres, telefono, correo } = datos;
    const nombreFinal = nombre || nombres || '';
    const apellidoFinal = apellido || '';
    const correoFinal = correo || null;

    const resultado = await db.run(
      'INSERT INTO Clientes (identificacion, nombre, apellido, telefono, correo) VALUES (?, ?, ?, ?, ?)',
      [identificacion, nombreFinal, apellidoFinal, telefono, correoFinal]
    );

    return await this.findById(resultado.lastID);
  }
}

export default new ClienteRepository();
