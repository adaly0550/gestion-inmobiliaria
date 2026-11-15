// src/models/Propiedad.js
// Modelo representativo de la tabla Propiedades
import { EstadoPropiedad } from '../constants/enums.js';

export class Propiedad {
  constructor({
    id = null,
    codigoInterno,
    direccion,
    tipo,
    modalidad,
    estado = EstadoPropiedad.DISPONIBLE
  }) {
    this.id = id;
    this.codigoInterno = codigoInterno;
    this.direccion = direccion;
    this.tipo = tipo;
    this.modalidad = modalidad; // 'VENTA' | 'ALQUILER'
    this.estado = estado; // 'DISPONIBLE' | 'RESERVADA' | 'VENDIDA' | 'ALQUILADA'
  }
}

export default Propiedad;
