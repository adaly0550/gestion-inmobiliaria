// src/models/index.js
// Punto de exportación central de los modelos del sistema
import { Usuario } from './Usuario.js';
import { Propiedad } from './Propiedad.js';
import { Cliente } from './Cliente.js';
import { Visita } from './Visita.js';
import { HistorialVisita } from './HistorialVisita.js';

export { Usuario, Propiedad, Cliente, Visita, HistorialVisita };

export default {
  Usuario,
  Propiedad,
  Cliente,
  Visita,
  HistorialVisita
};
