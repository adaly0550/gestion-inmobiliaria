// src/models/Usuario.js
// Modelo representativo de la tabla Usuarios
export class Usuario {
  constructor({ id = null, nombres, usuario, password, rol }) {
    this.id = id;
    this.nombres = nombres;
    this.usuario = usuario;
    this.password = password;
    this.rol = rol; // 'ADMINISTRADOR' | 'ASESOR'
  }
}

export default Usuario;
