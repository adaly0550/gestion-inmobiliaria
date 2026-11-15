import { EstadoPropiedad, Rol } from '../constants/enums.js';

// Datos iniciales de prueba
export const db = {
    usuarios: [
        { id: 1, nombres: 'Admin Principal', usuario: 'admin', rol: Rol.ADMINISTRADOR },
        { id: 2, nombres: 'Carlos Asesor', usuario: 'carlos', rol: Rol.ASESOR }
    ],
    propiedades: [
        { id: 1, codigoInterno: 'P-01', direccion: 'Av. Amazonas', tipo: 'Casa', modalidad: 'VENTA', estado: EstadoPropiedad.DISPONIBLE }
    ],
    clientes: [],
    visitas: [],
    historial: []
};