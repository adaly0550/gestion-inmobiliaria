// src/models/Visita.js
// Modelo representativo de la tabla Visitas y sus relaciones con Cliente, Propiedad y Usuario (Asesor)
import { EstadoVisita } from '../constants/enums.js';

export class Visita {
  constructor({
    id = null,
    idCliente,
    idPropiedad,
    idUsuario,
    fecha,
    horaInicio,
    horaFin,
    estado = EstadoVisita.PENDIENTE
  }) {
    this.id = id;
    this.idCliente = Number(idCliente); // FK -> Clientes(id)
    this.idPropiedad = Number(idPropiedad); // FK -> Propiedades(id)
    this.idUsuario = Number(idUsuario); // FK -> Usuarios(id)
    this.fecha = fecha;
    this.horaInicio = horaInicio;
    this.horaFin = horaFin;
    this.estado = estado; // 'PENDIENTE' | 'CANCELADA'
  }
}

export default Visita;
