// src/models/Cliente.js
// Modelo representativo de la tabla Clientes (RN-05)
export class Cliente {
  constructor({ id = null, identificacion, nombre, apellido = '', telefono, correo = null }) {
    this.id = id;
    this.identificacion = identificacion;
    this.nombre = nombre;
    this.apellido = apellido;
    this.telefono = telefono;
    this.correo = correo;
  }
}

export default Cliente;
