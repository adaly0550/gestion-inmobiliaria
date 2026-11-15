// src/models/HistorialVisita.js
// Modelo representativo de la tabla HistorialVisitas para auditoría (RNF-03 y HU-09)
export class HistorialVisita {
  constructor({
    id = null,
    idVisita,
    idUsuario,
    tipoCambio,
    motivo,
    fechaHoraCambio = new Date().toISOString(),
    fechaAnterior = null,
    fechaNueva = null
  }) {
    this.id = id;
    this.idVisita = Number(idVisita); // FK -> Visitas(id)
    this.idUsuario = Number(idUsuario); // FK -> Usuarios(id)
    this.tipoCambio = tipoCambio; // 'CANCELACION' | 'REPROGRAMACION' | 'CREACION'
    this.motivo = motivo;
    this.fechaHoraCambio = fechaHoraCambio;
    this.fechaAnterior = fechaAnterior;
    this.fechaNueva = fechaNueva;
  }
}

export default HistorialVisita;
