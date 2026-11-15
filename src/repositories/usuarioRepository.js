// src/repositories/usuarioRepository.js
import { conectarDB } from '../config/database.js';

class UsuarioRepository {
  async findById(id) {
    const db = await conectarDB();
    return (await db.get('SELECT id, nombres, usuario, rol FROM Usuarios WHERE id = ?', [Number(id)])) || null;
  }

  async findAll() {
    const db = await conectarDB();
    return await db.all('SELECT id, nombres, usuario, rol FROM Usuarios');
  }

  async findByUsuario(nombreUsuario) {
    const db = await conectarDB();
    return (await db.get('SELECT * FROM Usuarios WHERE usuario = ?', [nombreUsuario])) || null;
  }
}

export default new UsuarioRepository();
