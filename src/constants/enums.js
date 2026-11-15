export const EstadoPropiedad = Object.freeze({
    DISPONIBLE: 'DISPONIBLE',
    RESERVADA: 'RESERVADA',
    VENDIDA: 'VENDIDA',
    ALQUILADA: 'ALQUILADA'
});

export const EstadoVisita = Object.freeze({
    PENDIENTE: 'PENDIENTE',
    CANCELADA: 'CANCELADA'
});

export const Rol = Object.freeze({
    ASESOR: 'ASESOR',
    ADMINISTRADOR: 'ADMINISTRADOR'
});

export const TipoCambio = Object.freeze({
    CANCELACION: 'CANCELACION',
    REPROGRAMACION: 'REPROGRAMACION',
    CREACION: 'CREACION'
});

// Horario real de atención de la inmobiliaria (Lunes a Viernes 09:00-18:00, Sábados 09:00-13:00)
export const HorarioAtencion = Object.freeze({
    LUNES_A_VIERNES: { apertura: '09:00', cierre: '18:00' },
    SABADO: { apertura: '09:00', cierre: '13:00' }
});
