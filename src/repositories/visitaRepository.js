// src/repositories/visitaRepository.js
import { conectarDB } from '../config/database.js';

class VisitaRepository {
  async findById(id) {
    const db = await conectarDB();
    return (
      (await db.get(
        `SELECT v.*, 
                TRIM(c.nombre || ' ' || COALESCE(c.apellido, '')) AS clienteNombre,
                p.codigoInterno AS propiedadCodigo,
                p.direccion AS propiedadDireccion,
                u.nombres AS asesorNombre
         FROM Visitas v
         LEFT JOIN Clientes c ON v.idCliente = c.id
         LEFT JOIN Propiedades p ON v.idPropiedad = p.id
         LEFT JOIN Usuarios u ON v.idUsuario = u.id
         WHERE v.id = ?`,
        [Number(id)]
      )) || null
    );
  }

  async findByUsuarioIdAndFecha(usuarioId, fecha) {
    const db = await conectarDB();
    return await db.all(
      'SELECT * FROM Visitas WHERE idUsuario = ? AND fecha = ?',
      [Number(usuarioId), fecha]
    );
  }

  async findByClienteIdAndFecha(clienteId, fecha) {
    const db = await conectarDB();
    return await db.all(
      'SELECT * FROM Visitas WHERE idCliente = ? AND fecha = ?',
      [Number(clienteId), fecha]
    );
  }

  async findByPropiedadIdAndFecha(propiedadId, fecha) {
    const db = await conectarDB();
    return await db.all(
      'SELECT * FROM Visitas WHERE idPropiedad = ? AND fecha = ?',
      [Number(propiedadId), fecha]
    );
  }

  async findByPropiedadIdAndEstado(idPropiedad, estado) {
    const db = await conectarDB();
    return await db.all(
      'SELECT * FROM Visitas WHERE idPropiedad = ? AND estado = ?',
      [Number(idPropiedad), estado]
    );
  }

  async findByEstado(estado) {
    const db = await conectarDB();
    return await db.all(
      `SELECT v.*, 
              c.nombre || ' ' || COALESCE(c.apellido, '') AS clienteNombre,
              p.codigoInterno AS propiedadCodigo,
              p.direccion AS propiedadDireccion,
              u.nombres AS asesorNombre
       FROM Visitas v
       LEFT JOIN Clientes c ON v.idCliente = c.id
       LEFT JOIN Propiedades p ON v.idPropiedad = p.id
       LEFT JOIN Usuarios u ON v.idUsuario = u.id
       WHERE v.estado = ?
       ORDER BY v.fecha ASC, v.horaInicio ASC`,
      [estado]
    );
  }

  async findAll() {
    const db = await conectarDB();
    return await db.all(
      `SELECT v.*, 
              c.nombre || ' ' || COALESCE(c.apellido, '') AS clienteNombre,
              p.codigoInterno AS propiedadCodigo,
              p.direccion AS propiedadDireccion,
              u.nombres AS asesorNombre
       FROM Visitas v
       LEFT JOIN Clientes c ON v.idCliente = c.id
       LEFT JOIN Propiedades p ON v.idPropiedad = p.id
       LEFT JOIN Usuarios u ON v.idUsuario = u.id
       ORDER BY v.id DESC`
    );
  }

  async save(data) {
    const db = await conectarDB();
    const { idCliente, idPropiedad, idUsuario, fecha, horaInicio, horaFin, estado } = data;
    const resultado = await db.run(
      `INSERT INTO Visitas (idCliente, idPropiedad, idUsuario, fecha, horaInicio, horaFin, estado)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [Number(idCliente), Number(idPropiedad), Number(idUsuario), fecha, horaInicio, horaFin, estado]
    );
    return await this.findById(resultado.lastID);
  }

  async updateEstado(id, nuevoEstado) {
    const db = await conectarDB();
    await db.run('UPDATE Visitas SET estado = ? WHERE id = ?', [nuevoEstado, Number(id)]);
    return await this.findById(id);
  }
}

export default new VisitaRepository();
