// src/repositories/avisoRepository.js
import { conectarDB } from '../config/database.js';

class AvisoRepository {
  async save({ idUsuario, mensaje }) {
    const db = await conectarDB();
    const fechaHora = new Date().toISOString();
    const resultado = await db.run(
      'INSERT INTO Avisos (idUsuario, mensaje, fechaHora) VALUES (?, ?, ?)',
      [Number(idUsuario), mensaje, fechaHora]
    );
    return await db.get('SELECT * FROM Avisos WHERE id = ?', [resultado.lastID]);
  }

  async findByUsuarioId(idUsuario) {
    const db = await conectarDB();
    return await db.all(
      `SELECT a.*, u.nombres AS usuarioNombre
       FROM Avisos a
       LEFT JOIN Usuarios u ON a.idUsuario = u.id
       WHERE a.idUsuario = ?
       ORDER BY a.id DESC`,
      [Number(idUsuario)]
    );
  }

  async findAll() {
    const db = await conectarDB();
    return await db.all(
      `SELECT a.*, u.nombres AS usuarioNombre
       FROM Avisos a
       LEFT JOIN Usuarios u ON a.idUsuario = u.id
       ORDER BY a.id DESC`
    );
  }
}

export default new AvisoRepository();
