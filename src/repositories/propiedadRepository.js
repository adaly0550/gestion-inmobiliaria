// src/repositories/propiedadRepository.js
import { conectarDB } from '../config/database.js';
import { EstadoPropiedad } from '../constants/enums.js';

class PropiedadRepository {
  async findById(id) {
    const db = await conectarDB();
    return (await db.get('SELECT * FROM Propiedades WHERE id = ?', [Number(id)])) || null;
  }

  async findByCodigo(codigoInterno) {
    const db = await conectarDB();
    return (await db.get('SELECT * FROM Propiedades WHERE codigoInterno = ?', [codigoInterno])) || null;
  }

  async findDisponibles() {
    const db = await conectarDB();
    return await db.all('SELECT * FROM Propiedades WHERE estado = ? ORDER BY id DESC', [EstadoPropiedad.DISPONIBLE]);
  }

  async findAll() {
    const db = await conectarDB();
    return await db.all('SELECT * FROM Propiedades ORDER BY id DESC');
  }

  async save(data) {
    const db = await conectarDB();
    const { codigoInterno, direccion, tipo, modalidad, estado } = data;
    const resultado = await db.run(
      'INSERT INTO Propiedades (codigoInterno, direccion, tipo, modalidad, estado) VALUES (?, ?, ?, ?, ?)',
      [codigoInterno, direccion, tipo, modalidad, estado]
    );
    return await this.findById(resultado.lastID);
  }

  async updateEstado(id, nuevoEstado) {
    const db = await conectarDB();
    await db.run('UPDATE Propiedades SET estado = ? WHERE id = ?', [nuevoEstado, Number(id)]);
    return await this.findById(id);
  }
}

export default new PropiedadRepository();
