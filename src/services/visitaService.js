// src/services/visitaService.js
import visitaRepository from '../repositories/visitaRepository.js';
import propiedadRepository from '../repositories/propiedadRepository.js';
import clienteRepository from '../repositories/clienteRepository.js';
import historialVisitaRepository from '../repositories/historialVisitaRepository.js';
import avisoRepository from '../repositories/avisoRepository.js';
import { EstadoPropiedad, EstadoVisita, TipoCambio, Rol, HorarioAtencion } from '../constants/enums.js';

class VisitaService {
  // HU-05 / RF-05, RF-06, RF-07: Agendar visita
  async agendarVisita({ idCliente, idPropiedad, fecha, horaInicio }, usuarioSesion) {
    if (!idCliente || !idPropiedad || !fecha || !horaInicio) {
      throw new Error('Cliente, propiedad, fecha y hora de inicio son obligatorios.');
    }

    // Validar que el cliente exista
    const cliente = await clienteRepository.findById(idCliente);
    if (!cliente) {
      throw new Error('El cliente seleccionado no existe.');
    }

    // 1. RN-01: Validar estado de la propiedad
    const propiedad = await propiedadRepository.findById(idPropiedad);
    if (!propiedad) throw new Error('La propiedad no existe.');
    if (propiedad.estado !== EstadoPropiedad.DISPONIBLE) {
      throw new Error('RN-01: Solo una propiedad disponible puede recibir visitas.');
    }

    // 2. RN-04: Calcular hora_fin sumando exactamente 60 minutos
    if (!/^\d{2}:\d{2}$/.test(horaInicio)) {
      throw new Error('Formato de hora inválido. Use HH:MM (ej. 10:00).');
    }
    const [horas, minutos] = horaInicio.split(':').map(Number);
    if (horas < 0 || horas > 22 || minutos < 0 || minutos > 59) {
      throw new Error('Hora de inicio fuera de rango permitido.');
    }
    const inicioMinutos = horas * 60 + minutos;
    const finMinutos = inicioMinutos + 60;
    const horaFin = `${String(Math.floor(finMinutos / 60)).padStart(2, '0')}:${String(finMinutos % 60).padStart(2, '0')}`;

    // 2b. RN-11: Validar horario real de atención de la inmobiliaria
    this.validarHorarioAtencion(fecha, horaInicio, horaFin);

    // 3. RN-04: Verificar cruces de horario (Asesor, Cliente y Propiedad)
    const hayCruce = (visitas) =>
      visitas.some((v) => v.estado !== EstadoVisita.CANCELADA && horaInicio < v.horaFin && horaFin > v.horaInicio);

    // 3a. Cruce de horario para el mismo asesor
    const visitasAsesor = await visitaRepository.findByUsuarioIdAndFecha(usuarioSesion.id, fecha);
    if (hayCruce(visitasAsesor)) {
      throw new Error('RN-04: El asesor ya tiene una visita programada que se cruza en ese horario.');
    }

    // 3b. Cruce de horario para el mismo cliente (no puede estar en 2 propiedades al mismo tiempo)
    const visitasCliente = await visitaRepository.findByClienteIdAndFecha(idCliente, fecha);
    if (hayCruce(visitasCliente)) {
      throw new Error('RN-04: El cliente ya tiene otra visita programada en ese horario (no puede estar en dos propiedades a la misma hora).');
    }

    // 3c. Duplicado o cruce en la misma propiedad
    const visitasPropiedad = await visitaRepository.findByPropiedadIdAndFecha(idPropiedad, fecha);
    const mismaPropiedadMismoCliente = visitasPropiedad.some(
      (v) => v.estado !== EstadoVisita.CANCELADA && v.idCliente === Number(idCliente)
    );
    if (mismaPropiedadMismoCliente) {
      throw new Error('Este cliente ya tiene una visita pendiente en esta misma propiedad para esa fecha.');
    }
    if (hayCruce(visitasPropiedad)) {
      throw new Error('La propiedad ya tiene otra visita programada que se cruza en ese horario.');
    }

    // 4. Guardar visita
    const nuevaVisita = await visitaRepository.save({
      idCliente,
      idPropiedad,
      idUsuario: usuarioSesion.id,
      fecha,
      horaInicio,
      horaFin,
      estado: EstadoVisita.PENDIENTE
    });

    // 5. RN-10: Generar aviso interno
    await this.generarAvisoInterno(
      nuevaVisita,
      `Nueva visita agendada (${propiedad.codigoInterno}) para el ${fecha} de ${horaInicio} a ${horaFin}.`
    );

    return nuevaVisita;
  }

  // HU-06 / RF-08: Cancelar visita
  async cancelarVisita(idVisita, motivo, usuarioSesion) {
    if (!motivo || motivo.trim() === '') {
      throw new Error('RN-08: Se requiere un motivo obligatorio para cancelar la visita.');
    }

    const visita = await visitaRepository.findById(idVisita);
    if (!visita) throw new Error('Visita no encontrada.');

    if (visita.estado === EstadoVisita.CANCELADA) {
      throw new Error('La visita ya se encuentra cancelada.');
    }

    // RN-07: Asesor solo maneja sus visitas
    if (usuarioSesion.rol === Rol.ASESOR && visita.idUsuario !== usuarioSesion.id) {
      throw new Error('RN-07: Un asesor solo puede cancelar sus propias visitas.');
    }

    // Actualizar estado sin borrar la fila (RNF-03)
    await visitaRepository.updateEstado(idVisita, EstadoVisita.CANCELADA);

    // Guardar en Historial (HU-09)
    await historialVisitaRepository.save({
      idVisita,
      idUsuario: usuarioSesion.id,
      tipoCambio: TipoCambio.CANCELACION,
      motivo: motivo.trim(),
      fechaHoraCambio: new Date(),
      fechaAnterior: visita.fecha,
      fechaNueva: null
    });

    await this.generarAvisoInterno(visita, `Visita #${idVisita} cancelada. Motivo: ${motivo.trim()}`);

    return { mensaje: 'Visita cancelada e historial registrado exitosamente.' };
  }

  // RF-09 / RN-02: Cancelación automática por cambio de estado de propiedad
  async cancelarVisitasPorPropiedad(idPropiedad, motivo, usuarioResponsable = null) {
    const pendientes = await visitaRepository.findByPropiedadIdAndEstado(
      idPropiedad,
      EstadoVisita.PENDIENTE
    );

    for (const v of pendientes) {
      await visitaRepository.updateEstado(v.id, EstadoVisita.CANCELADA);
      await historialVisitaRepository.save({
        idVisita: v.id,
        idUsuario: usuarioResponsable?.id || v.idUsuario,
        tipoCambio: TipoCambio.CANCELACION,
        motivo,
        fechaHoraCambio: new Date(),
        fechaAnterior: v.fecha,
        fechaNueva: null
      });
      await this.generarAvisoInterno(v, `Visita #${v.id} cancelada automáticamente: ${motivo}`);
    }
  }

  // Listar visitas pendientes (filtrado por rol RN-07 y fecha opcional)
  async listarPendientes(fecha, usuarioSesion) {
    let pendientes = await visitaRepository.findByEstado(EstadoVisita.PENDIENTE);
    if (usuarioSesion && usuarioSesion.rol === Rol.ASESOR) {
      pendientes = pendientes.filter((v) => v.idUsuario === usuarioSesion.id);
    }
    if (fecha) {
      pendientes = pendientes.filter((v) => v.fecha === fecha);
    }
    return pendientes;
  }

  // Listar todas las visitas (incluye canceladas por RNF-03, filtradas por rol RN-07)
  async listarTodas(fecha, usuarioSesion) {
    let visitas = await visitaRepository.findAll();
    if (usuarioSesion && usuarioSesion.rol === Rol.ASESOR) {
      visitas = visitas.filter((v) => v.idUsuario === usuarioSesion.id);
    }
    if (fecha) {
      visitas = visitas.filter((v) => v.fecha === fecha);
    }
    return visitas;
  }

  validarHorarioAtencion(fecha, horaInicio, horaFin) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(fecha)) {
      throw new Error('Formato de fecha inválido. Use YYYY-MM-DD.');
    }

    const fechaObj = new Date(`${fecha}T00:00:00`);
    if (Number.isNaN(fechaObj.getTime())) {
      throw new Error('La fecha ingresada no es válida.');
    }

    // No agendar en fechas u horas pasadas
    const ahora = new Date();
    const hoyStr = `${ahora.getFullYear()}-${String(ahora.getMonth() + 1).padStart(2, '0')}-${String(ahora.getDate()).padStart(2, '0')}`;
    const horaActualStr = `${String(ahora.getHours()).padStart(2, '0')}:${String(ahora.getMinutes()).padStart(2, '0')}`;

    if (fecha < hoyStr || (fecha === hoyStr && horaInicio <= horaActualStr)) {
      throw new Error('Horario de atención: No se pueden agendar visitas en fechas u horas pasadas.');
    }

    const diaSemana = fechaObj.getDay(); // 0 = Domingo, 6 = Sábado

    // Domingos cerrado
    if (diaSemana === 0) {
      throw new Error('Horario de atención: La inmobiliaria no atiende los días domingo.');
    }

    // Sábados: 09:00 a 13:00
    if (diaSemana === 6) {
      const { apertura, cierre } = HorarioAtencion.SABADO;
      if (horaInicio < apertura || horaFin > cierre) {
        throw new Error(`Horario de atención los sábados: ${apertura} a ${cierre} (última cita inicia a las 12:00).`);
      }
      return;
    }

    // Lunes a Viernes: 09:00 a 18:00
    const { apertura, cierre } = HorarioAtencion.LUNES_A_VIERNES;
    if (horaInicio < apertura || horaFin > cierre) {
      throw new Error(`Horario de atención de lunes a viernes: ${apertura} a ${cierre} (última cita inicia a las 17:00).`);
    }
  }

  async generarAvisoInterno(visita, mensaje) {
    console.log(`[AVISO INTERNO - ASESOR ${visita.idUsuario}]: ${mensaje}`);
    await avisoRepository.save({
      idUsuario: visita.idUsuario,
      mensaje
    });
  }
}

export default new VisitaService();
