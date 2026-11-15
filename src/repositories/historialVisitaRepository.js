// src/repositories/historialVisitaRepository.js
import { conectarDB } from '../config/database.js';

class HistorialVisitaRepository {
  async save(data) {
    const db = await conectarDB();
    const {
      idVisita,
      idUsuario,
      tipoCambio,
      motivo,
      fechaHoraCambio,
      fechaAnterior,
      fechaNueva
    } = data;

    const fechaStr =
      fechaHoraCambio instanceof Date ? fechaHoraCambio.toISOString() : String(fechaHoraCambio);

    const resultado = await db.run(
      `INSERT INTO HistorialVisitas (idVisita, idUsuario, tipoCambio, motivo, fechaHoraCambio, fechaAnterior, fechaNueva)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [Number(idVisita), Number(idUsuario), tipoCambio, motivo, fechaStr, fechaAnterior, fechaNueva]
    );

    return await db.get('SELECT * FROM HistorialVisitas WHERE id = ?', [resultado.lastID]);
  }

  async findByVisitaId(idVisita) {
    const db = await conectarDB();
    return await db.all(
      `SELECT h.*, u.nombres AS usuarioNombre, u.rol AS usuarioRol
       FROM HistorialVisitas h
       LEFT JOIN Usuarios u ON h.idUsuario = u.id
       WHERE h.idVisita = ?
       ORDER BY h.id DESC`,
      [Number(idVisita)]
    );
  }

  async findAll() {
    const db = await conectarDB();
    return await db.all(
      `SELECT h.*, 
              u.nombres AS usuarioNombre, 
              u.rol AS usuarioRol,
              v.fecha AS fechaVisita,
              p.codigoInterno AS propiedadCodigo
       FROM HistorialVisitas h
       LEFT JOIN Usuarios u ON h.idUsuario = u.id
       LEFT JOIN Visitas v ON h.idVisita = v.id
       LEFT JOIN Propiedades p ON v.idPropiedad = p.id
       ORDER BY h.id DESC`
    );
  }
}

export default new HistorialVisitaRepository();
