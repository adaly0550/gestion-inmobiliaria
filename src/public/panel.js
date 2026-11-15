// src/public/panel.js - Lógica compartida y sencilla para los paneles de Admin y Asesor

const sesionRaw = localStorage.getItem('usuarioSesion');
if (!sesionRaw) {
  window.location.href = '/index.html';
}
const usuarioSesion = JSON.parse(sesionRaw || '{}');

// Estado global de paginación por pestaña (5 elementos por página)
const POR_PAGINA = 5;
const paginas = {
  propiedades: 1,
  clientes: 1,
  visitas: 1,
  auditoria: 1,
  avisos: 1
};

let cachePropiedades = [];
let cacheClientes = [];
let cacheVisitas = [];
let cacheAuditoria = [];
let cacheAvisos = [];

// 1. Verificar que el usuario tenga permiso para ver esta página (RN-06)
window.iniciarPanel = async (rolPermitido) => {
  if (!usuarioSesion || !usuarioSesion.id) {
    window.location.href = '/index.html';
    return;
  }

  if (usuarioSesion.rol !== rolPermitido) {
    // Redirigir automáticamente a su panel correspondiente
    window.location.href = usuarioSesion.rol === 'ADMINISTRADOR' ? '/admin.html' : '/asesor.html';
    return;
  }

  document.getElementById('nombre-usuario').textContent = usuarioSesion.nombres;
  const badge = document.getElementById('rol-usuario');
  badge.textContent = usuarioSesion.rol;
  if (usuarioSesion.rol === 'ADMINISTRADOR') badge.classList.add('admin');

  await cargarTodo();
};

window.cerrarSesion = () => {
  localStorage.removeItem('usuarioSesion');
  window.location.href = '/index.html';
};

// 2. Manejo de pestañas
window.cambiarPestana = (idPestana, boton) => {
  document.querySelectorAll('.tab-content').forEach((el) => el.classList.remove('active'));
  document.querySelectorAll('.tab-btn').forEach((btn) => btn.classList.remove('active'));

  document.getElementById(idPestana).classList.add('active');
  if (boton) boton.classList.add('active');
};

// 3. Helper de peticiones HTTP con el header de sesión (x-usuario-id)
async function apiFetch(url, opciones = {}) {
  const headers = {
    'Content-Type': 'application/json',
    'x-usuario-id': String(usuarioSesion.id),
    ...(opciones.headers || {})
  };
  const respuesta = await fetch(url, { ...opciones, headers });
  const data = await respuesta.json();
  if (!respuesta.ok) {
    throw new Error(data.error || data.mensaje || 'Error en la petición');
  }
  return data;
}

// 4. Mostrar mensajes en pantalla
function mostrarAlerta(mensaje, tipo = 'success') {
  const alerta = document.getElementById('alerta-global');
  alerta.textContent = mensaje;
  alerta.className = `alert ${tipo}`;
  setTimeout(() => {
    alerta.className = 'alert';
    alerta.textContent = '';
  }, 5000);
}

// 5. Función genérica de paginación
function paginar(lista, seccion) {
  const totalPaginas = Math.max(1, Math.ceil(lista.length / POR_PAGINA));
  if (paginas[seccion] > totalPaginas) paginas[seccion] = totalPaginas;
  if (paginas[seccion] < 1) paginas[seccion] = 1;

  const inicio = (paginas[seccion] - 1) * POR_PAGINA;
  const items = lista.slice(inicio, inicio + POR_PAGINA);

  const contenedor = document.getElementById(`pag-${seccion}`);
  if (contenedor) {
    contenedor.innerHTML = `
      <span>Mostrando ${items.length} de ${lista.length} registros (Página ${paginas[seccion]} de ${totalPaginas})</span>
      <div>
        <button class="btn btn-secondary btn-small" ${paginas[seccion] <= 1 ? 'disabled' : ''} onclick="moverPagina('${seccion}', -1)">Anterior</button>
        <button class="btn btn-secondary btn-small" ${paginas[seccion] >= totalPaginas ? 'disabled' : ''} onclick="moverPagina('${seccion}', 1)">Siguiente</button>
      </div>
    `;
  }
  return items;
}

window.moverPagina = (seccion, cambio) => {
  paginas[seccion] += cambio;
  if (seccion === 'propiedades') renderPropiedades();
  if (seccion === 'clientes') renderClientes();
  if (seccion === 'visitas') renderVisitas();
  if (seccion === 'auditoria') renderAuditoria();
  if (seccion === 'avisos') renderAvisos();
};

// Cargar todos los datos del usuario según su rol
async function cargarTodo() {
  await cargarPropiedades();
  await cargarClientes();
  await cargarVisitas();
  await cargarAvisos();
  if (usuarioSesion.rol === 'ADMINISTRADOR') {
    await cargarAuditoria();
  }
}

// ================= PROPIEDADES =================
async function cargarPropiedades() {
  try {
    const endpoint = usuarioSesion.rol === 'ADMINISTRADOR' ? '/api/propiedades' : '/api/propiedades/disponibles';
    cachePropiedades = await apiFetch(endpoint);
    renderPropiedades();
    actualizarSelectPropiedades();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
}

function renderPropiedades() {
  const tbody = document.getElementById('tabla-propiedades');
  const items = paginar(cachePropiedades, 'propiedades');
  tbody.innerHTML = '';

  if (items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">No hay propiedades registradas.</td></tr>';
    return;
  }

  items.forEach((p) => {
    const esAdmin = usuarioSesion.rol === 'ADMINISTRADOR';
    const accionHtml = esAdmin
      ? `<select onchange="cambiarEstadoPropiedad(${p.id}, this.value)" style="width:auto; padding:4px;">
           <option value="DISPONIBLE" ${p.estado === 'DISPONIBLE' ? 'selected' : ''}>DISPONIBLE</option>
           <option value="RESERVADA" ${p.estado === 'RESERVADA' ? 'selected' : ''}>RESERVADA</option>
           <option value="VENDIDA" ${p.estado === 'VENDIDA' ? 'selected' : ''}>VENDIDA</option>
           <option value="ALQUILADA" ${p.estado === 'ALQUILADA' ? 'selected' : ''}>ALQUILADA</option>
         </select>`
      : '-';

    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>${p.id}</td>
      <td><strong>${p.codigoInterno}</strong></td>
      <td>${p.direccion}</td>
      <td>${p.tipo}</td>
      <td>${p.modalidad}</td>
      <td><span class="estado estado-${p.estado}">${p.estado}</span></td>
      ${esAdmin ? `<td>${accionHtml}</td>` : ''}
    `;
    tbody.appendChild(fila);
  });
}

window.registrarPropiedad = async (e) => {
  e.preventDefault();
  try {
    const body = {
      codigoInterno: document.getElementById('prop-codigo').value.trim(),
      direccion: document.getElementById('prop-direccion').value.trim(),
      tipo: document.getElementById('prop-tipo').value.trim(),
      modalidad: document.getElementById('prop-modalidad').value
    };
    await apiFetch('/api/propiedades', { method: 'POST', body: JSON.stringify(body) });
    e.target.reset();
    mostrarAlerta('Propiedad registrada correctamente.');
    await cargarPropiedades();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
};

window.cambiarEstadoPropiedad = async (id, nuevoEstado) => {
  try {
    await apiFetch(`/api/propiedades/${id}/estado`, {
      method: 'PATCH',
      body: JSON.stringify({ estado: nuevoEstado })
    });
    mostrarAlerta(`Estado actualizado a ${nuevoEstado}. Si tenía visitas pendientes, fueron canceladas automáticamente (RN-02).`);
    await cargarTodo();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
    await cargarPropiedades();
  }
};

function actualizarSelectPropiedades() {
  const select = document.getElementById('visita-propiedad');
  if (!select) return;
  const disponibles = cachePropiedades.filter((p) => p.estado === 'DISPONIBLE');
  select.innerHTML = '<option value="">-- Seleccione propiedad disponible --</option>';
  disponibles.forEach((p) => {
    select.innerHTML += `<option value="${p.id}">${p.codigoInterno} - ${p.direccion} (${p.tipo})</option>`;
  });
}

// ================= CLIENTES =================
async function cargarClientes() {
  try {
    cacheClientes = await apiFetch('/api/clientes');
    renderClientes();
    actualizarSelectClientes();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
}

function renderClientes() {
  const tbody = document.getElementById('tabla-clientes');
  const items = paginar(cacheClientes, 'clientes');
  tbody.innerHTML = '';

  if (items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">No hay clientes registrados.</td></tr>';
    return;
  }

  items.forEach((c) => {
    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>${c.id}</td>
      <td>${c.identificacion}</td>
      <td>${c.nombre || ''}</td>
      <td>${c.apellido || ''}</td>
      <td>${c.telefono}</td>
      <td>${c.correo || '-'}</td>
    `;
    tbody.appendChild(fila);
  });
}

window.registrarCliente = async (e) => {
  e.preventDefault();
  try {
    const body = {
      identificacion: document.getElementById('cli-identificacion').value.trim(),
      nombre: document.getElementById('cli-nombre').value.trim(),
      apellido: document.getElementById('cli-apellido').value.trim(),
      telefono: document.getElementById('cli-telefono').value.trim(),
      correo: document.getElementById('cli-correo').value.trim()
    };
    await apiFetch('/api/clientes', { method: 'POST', body: JSON.stringify(body) });
    e.target.reset();
    mostrarAlerta('Cliente guardado correctamente en SQLite.');
    await cargarClientes();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
};

function actualizarSelectClientes() {
  const select = document.getElementById('visita-cliente');
  if (!select) return;
  select.innerHTML = '<option value="">-- Seleccione un cliente --</option>';
  cacheClientes.forEach((c) => {
    select.innerHTML += `<option value="${c.id}">${c.nombre} ${c.apellido || ''} (${c.identificacion})</option>`;
  });
}

// ================= VISITAS =================
async function cargarVisitas() {
  try {
    const fechaFiltro = document.getElementById('filtro-fecha-visita')?.value || '';
    const url = fechaFiltro ? `/api/visitas?fecha=${fechaFiltro}` : '/api/visitas';
    cacheVisitas = await apiFetch(url);
    renderVisitas();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
}

window.filtrarVisitas = () => {
  paginas.visitas = 1;
  cargarVisitas();
};

function renderVisitas() {
  const tbody = document.getElementById('tabla-visitas');
  const items = paginar(cacheVisitas, 'visitas');
  tbody.innerHTML = '';

  if (items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">No hay visitas registradas.</td></tr>';
    return;
  }

  items.forEach((v) => {
    const botonCancelar =
      v.estado === 'PENDIENTE'
        ? `<button class="btn btn-danger" onclick="cancelarVisita(${v.id})">Cancelar</button>`
        : '<span style="color:#888; font-size:12px;">Sin acciones</span>';

    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>${v.id}</td>
      <td>${v.clienteNombre || 'Cliente #' + v.idCliente}</td>
      <td>${v.propiedadCodigo ? `${v.propiedadCodigo} (${v.propiedadDireccion})` : 'Prop #' + v.idPropiedad}</td>
      <td>${v.asesorNombre || 'Asesor #' + v.idUsuario}</td>
      <td>${v.fecha}</td>
      <td>${v.horaInicio} - ${v.horaFin}</td>
      <td><span class="estado estado-${v.estado}">${v.estado}</span></td>
      <td>${botonCancelar}</td>
    `;
    tbody.appendChild(fila);
  });
}

window.agendarVisita = async (e) => {
  e.preventDefault();
  try {
    const body = {
      idCliente: Number(document.getElementById('visita-cliente').value),
      idPropiedad: Number(document.getElementById('visita-propiedad').value),
      fecha: document.getElementById('visita-fecha').value,
      horaInicio: document.getElementById('visita-hora').value
    };
    await apiFetch('/api/visitas', { method: 'POST', body: JSON.stringify(body) });
    e.target.reset();
    mostrarAlerta('Visita agendada por 60 minutos (RN-04) y aviso interno generado (RN-10).');
    await cargarTodo();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
};

window.cancelarVisita = async (idVisita) => {
  // RN-08: Motivo obligatorio para cancelar
  const motivo = prompt('RN-08: Ingresa el motivo obligatorio para cancelar esta visita:');
  if (motivo === null) return; // Usuario canceló el diálogo
  if (motivo.trim() === '') {
    mostrarAlerta('RN-08: El motivo es obligatorio para cancelar una visita.', 'error');
    return;
  }

  try {
    await apiFetch(`/api/visitas/${idVisita}/cancelar`, {
      method: 'PATCH',
      body: JSON.stringify({ motivo })
    });
    mostrarAlerta('Visita cancelada (RNF-03: registro conservado) e historial guardado.');
    await cargarTodo();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
};

// ================= AUDITORÍA (Solo Admin - HU-09 / RN-06) =================
async function cargarAuditoria() {
  try {
    cacheAuditoria = await apiFetch('/api/auditor');
    renderAuditoria();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
}

function renderAuditoria() {
  const tbody = document.getElementById('tabla-auditoria');
  if (!tbody) return;
  const items = paginar(cacheAuditoria, 'auditoria');
  tbody.innerHTML = '';

  if (items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Aún no existen movimientos registrados en auditoría.</td></tr>';
    return;
  }

  items.forEach((h) => {
    const fechaFormateada = new Date(h.fechaHoraCambio).toLocaleString();
    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>${h.id}</td>
      <td>Visita #${h.idVisita}</td>
      <td>${h.usuarioNombre || 'Usuario #' + h.idUsuario} (${h.usuarioRol || ''})</td>
      <td><strong>${h.tipoCambio}</strong></td>
      <td>${h.motivo}</td>
      <td>${fechaFormateada}</td>
    `;
    tbody.appendChild(fila);
  });
}

// ================= AVISOS INTERNOS (RN-10) =================
async function cargarAvisos() {
  try {
    cacheAvisos = await apiFetch('/api/avisos');
    const contador = document.getElementById('contador-avisos');
    if (contador) contador.textContent = cacheAvisos.length;
    renderAvisos();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
}

function renderAvisos() {
  const tbody = document.getElementById('tabla-avisos');
  if (!tbody) return;
  const items = paginar(cacheAvisos, 'avisos');
  tbody.innerHTML = '';

  if (items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;">No hay avisos internos generados todavía.</td></tr>';
    return;
  }

  items.forEach((a) => {
    const fechaFormateada = new Date(a.fechaHora).toLocaleString();
    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>${a.id}</td>
      <td>${a.usuarioNombre || 'Asesor #' + a.idUsuario}</td>
      <td>${a.mensaje}</td>
      <td>${fechaFormateada}</td>
    `;
    tbody.appendChild(fila);
  });
}
