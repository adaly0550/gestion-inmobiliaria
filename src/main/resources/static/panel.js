// src/main/resources/static/panel.js - Lógica compartida para los paneles de Admin y Asesor
// Cumple los puntos 1, 2, 3, 4, 5, 6, 8, 11, 12 y 31

let usuarioSesion = JSON.parse(localStorage.getItem('usuarioSesion') || '{}');

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
let cacheAsesores = [];
let cacheVisitas = [];
let cacheAuditoria = [];
let cacheAvisos = [];

// Punto 11: Función para escapar cualquier dato antes de interpolarlo en HTML (prevención de XSS)
function escapeHtml(valor) {
  if (valor === null || valor === undefined) return '';
  return String(valor)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

// 1. Verificar sesión real contra el servidor (Punto 8 / RNF-01 / RNF-02)
window.iniciarPanel = async (rolPermitido) => {
  try {
    usuarioSesion = await apiFetch('/api/auth/me');
    localStorage.setItem('usuarioSesion', JSON.stringify(usuarioSesion));
  } catch (_) {
    localStorage.removeItem('usuarioSesion');
    window.location.href = '/index.html';
    return;
  }

  if (usuarioSesion.rol !== rolPermitido) {
    window.location.href = usuarioSesion.rol === 'ADMINISTRADOR' ? '/admin.html' : '/asesor.html';
    return;
  }

  document.getElementById('nombre-usuario').textContent = usuarioSesion.nombres;
  const badge = document.getElementById('rol-usuario');
  badge.textContent = usuarioSesion.rol;
  if (usuarioSesion.rol === 'ADMINISTRADOR') badge.classList.add('admin');

  // Si otra pestaña cambia la sesión activa en el navegador, sincronizar al volver a enfocar la ventana
  window.addEventListener('focus', async () => {
    try {
      const actual = await fetch('/api/auth/me', { credentials: 'same-origin' }).then((r) => (r.ok ? r.json() : null));
      if (actual && actual.rol !== rolPermitido) {
        window.location.href = actual.rol === 'ADMINISTRADOR' ? '/admin.html' : '/asesor.html';
      } else if (actual && Number(actual.id) !== Number(usuarioSesion.id)) {
        usuarioSesion = actual;
        localStorage.setItem('usuarioSesion', JSON.stringify(actual));
        document.getElementById('nombre-usuario').textContent = usuarioSesion.nombres;
        await cargarTodo();
      }
    } catch (_) {
      // Ignorar si no hay conexión momentánea
    }
  });

  await cargarTodo();
};

window.cerrarSesion = async () => {
  try {
    await fetch('/api/auth/logout', { method: 'POST', credentials: 'same-origin' });
  } catch (_) {
    // Ignorar error de red al cerrar sesión
  }
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

// 3. Helper de peticiones HTTP con cookie de sesión (Punto 8: sin cabecera x-usuario-id)
async function apiFetch(url, opciones = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(opciones.headers || {})
  };
  const respuesta = await fetch(url, {
    ...opciones,
    headers,
    credentials: 'same-origin'
  });
  const data = await respuesta.json().catch(() => ({}));
  if (!respuesta.ok) {
    if (respuesta.status === 401 && !url.includes('/api/auth/')) {
      localStorage.removeItem('usuarioSesion');
      window.location.href = '/index.html';
      return data;
    }
    if (respuesta.status === 403 && !url.includes('/api/auth/')) {
      // Verificar si la sesión cambió de rol en otra pestaña para redirigir sin mostrar alerta falsa
      const me = await fetch('/api/auth/me', { credentials: 'same-origin' }).then((r) => (r.ok ? r.json() : null)).catch(() => null);
      if (me && me.rol !== usuarioSesion.rol) {
        window.location.href = me.rol === 'ADMINISTRADOR' ? '/admin.html' : '/asesor.html';
        return data;
      }
    }
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
  if (usuarioSesion.rol === 'ADMINISTRADOR') {
    await cargarAsesores();
  }
  await cargarVisitas();
  await cargarAvisos();
  if (usuarioSesion.rol === 'ADMINISTRADOR') {
    await cargarAuditoria();
  }
}

// ================= ASESORES (Puntos 5 y 12) =================
async function cargarAsesores() {
  try {
    cacheAsesores = await apiFetch('/api/usuarios/asesores');
    const selectAgendar = document.getElementById('visita-asesor');
    if (selectAgendar) {
      selectAgendar.innerHTML = '<option value="">-- Seleccione un asesor --</option>';
      cacheAsesores.forEach((a) => {
        const opt = document.createElement('option');
        opt.value = a.id;
        opt.textContent = `${a.nombres} (${a.usuario})`;
        selectAgendar.appendChild(opt);
      });
    }

    const selectFiltro = document.getElementById('filtro-asesor-visita');
    if (selectFiltro) {
      const valorPrevio = selectFiltro.value;
      selectFiltro.innerHTML = '<option value="">-- Todos los asesores --</option>';
      cacheAsesores.forEach((a) => {
        const opt = document.createElement('option');
        opt.value = a.id;
        opt.textContent = `${a.nombres} (${a.usuario})`;
        selectFiltro.appendChild(opt);
      });
      selectFiltro.value = valorPrevio;
    }
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
}

// ================= PROPIEDADES (Puntos 4, 13, 25) =================
async function cargarPropiedades() {
  try {
    const endpoint =
      usuarioSesion.rol === 'ADMINISTRADOR'
        ? '/api/propiedades'
        : '/api/propiedades?estado=DISPONIBLE';
    cachePropiedades = await apiFetch(endpoint);
    renderPropiedades();
    actualizarSelectPropiedades();
    actualizarProximoCodigoPropiedad();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
}

function actualizarProximoCodigoPropiedad() {
  const inputCodigo = document.getElementById('prop-codigo');
  if (!inputCodigo) return;
  let maxNum = 0;
  cachePropiedades.forEach((p) => {
    const m = /^P-(\d+)$/i.exec(p.codigoInterno || '');
    if (m) {
      const n = parseInt(m[1], 10);
      if (n > maxNum) maxNum = n;
    }
  });
  const siguiente = String(maxNum + 1).padStart(2, '0');
  inputCodigo.value = `P-${siguiente} (Automático)`;
}

function renderPropiedades() {
  const tbody = document.getElementById('tabla-propiedades');
  const items = paginar(cachePropiedades, 'propiedades');
  tbody.innerHTML = '';

  const esAdmin = usuarioSesion.rol === 'ADMINISTRADOR';
  const totalCols = esAdmin ? 8 : 7;

  if (items.length === 0) {
    tbody.innerHTML = `<tr><td colspan="${totalCols}" style="text-align:center;">No hay propiedades registradas.</td></tr>`;
    return;
  }

  items.forEach((p) => {
    const estadoSafe = escapeHtml(p.estado);
    const accionHtml = esAdmin
      ? `<select onchange="cambiarEstadoPropiedad(${Number(p.id)}, this.value)" style="width:auto; padding:4px;">
           <option value="DISPONIBLE" ${p.estado === 'DISPONIBLE' ? 'selected' : ''}>DISPONIBLE</option>
           <option value="RESERVADA" ${p.estado === 'RESERVADA' ? 'selected' : ''}>RESERVADA</option>
           <option value="VENDIDA" ${p.estado === 'VENDIDA' ? 'selected' : ''}>VENDIDA</option>
           <option value="ALQUILADA" ${p.estado === 'ALQUILADA' ? 'selected' : ''}>ALQUILADA</option>
         </select>`
      : '-';

    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>${Number(p.id)}</td>
      <td><strong>${escapeHtml(p.codigoInterno)}</strong></td>
      <td>${escapeHtml(p.direccion)}</td>
      <td>${escapeHtml(p.tipo)}</td>
      <td>${escapeHtml(p.modalidad)}</td>
      <td>${escapeHtml(p.descripcion || '-')}</td>
      <td><span class="estado estado-${estadoSafe}">${estadoSafe}</span></td>
      ${esAdmin ? `<td>${accionHtml}</td>` : ''}
    `;
    tbody.appendChild(fila);
  });
}

window.registrarPropiedad = async (e) => {
  e.preventDefault();
  try {
    const body = {
      direccion: document.getElementById('prop-direccion').value.trim(),
      tipo: document.getElementById('prop-tipo').value.trim(),
      modalidad: document.getElementById('prop-modalidad').value,
      descripcion: document.getElementById('prop-descripcion')?.value.trim() || null
    };
    const creada = await apiFetch('/api/propiedades', { method: 'POST', body: JSON.stringify(body) });
    e.target.reset();
    mostrarAlerta(`Propiedad registrada correctamente con código automático ${creada.codigoInterno}.`);
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
    mostrarAlerta(`Estado actualizado a ${nuevoEstado}. Si tenía visitas futuras pendientes, fueron canceladas automáticamente (RN-02).`);
    await cargarTodo();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
    await cargarPropiedades();
  }
};

function actualizarSelectPropiedades() {
  const select = document.getElementById('visita-propiedad');
  if (select) {
    const disponibles = cachePropiedades.filter((p) => p.estado === 'DISPONIBLE');
    select.innerHTML = '<option value="">-- Seleccione propiedad disponible --</option>';
    disponibles.forEach((p) => {
      const opt = document.createElement('option');
      opt.value = p.id;
      opt.textContent = `${p.codigoInterno} - ${p.direccion} (${p.tipo})`;
      select.appendChild(opt);
    });
  }

  // Selector de filtro por propiedad en Admin (Punto 3 / HU-10)
  const selectFiltroProp = document.getElementById('filtro-propiedad-visita');
  if (selectFiltroProp) {
    const prev = selectFiltroProp.value;
    selectFiltroProp.innerHTML = '<option value="">-- Todas las propiedades --</option>';
    cachePropiedades.forEach((p) => {
      const opt = document.createElement('option');
      opt.value = p.id;
      opt.textContent = `${p.codigoInterno} - ${p.direccion} [${p.estado}]`;
      selectFiltroProp.appendChild(opt);
    });
    selectFiltroProp.value = prev;
  }
}

// ================= CLIENTES (RN-05 y Punto 11) =================
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
      <td>${Number(c.id)}</td>
      <td>${escapeHtml(c.identificacion)}</td>
      <td>${escapeHtml(c.nombre || '')}</td>
      <td>${escapeHtml(c.apellido || '')}</td>
      <td>${escapeHtml(c.telefono)}</td>
      <td>${escapeHtml(c.correo || '-')}</td>
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
    mostrarAlerta('Cliente guardado correctamente.');
    await cargarClientes();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
};

function actualizarSelectClientes() {
  const select = document.getElementById('visita-cliente');
  if (select) {
    select.innerHTML = '<option value="">-- Seleccione un cliente --</option>';
    cacheClientes.forEach((c) => {
      const opt = document.createElement('option');
      opt.value = c.id;
      opt.textContent = `${c.nombre} ${c.apellido || ''} (${c.identificacion})`.trim();
      select.appendChild(opt);
    });
  }

  // Selector de filtro por cliente en Admin (Punto 3 / HU-10)
  const selectFiltroCli = document.getElementById('filtro-cliente-visita');
  if (selectFiltroCli) {
    const prev = selectFiltroCli.value;
    selectFiltroCli.innerHTML = '<option value="">-- Todos los clientes --</option>';
    cacheClientes.forEach((c) => {
      const opt = document.createElement('option');
      opt.value = c.id;
      opt.textContent = `${c.nombre} ${c.apellido || ''} (${c.identificacion})`.trim();
      selectFiltroCli.appendChild(opt);
    });
    selectFiltroCli.value = prev;
  }
}

// ================= VISITAS (Puntos 1, 2, 3, 5, 12, 31) =================
async function cargarVisitas() {
  try {
    const modo = document.getElementById('filtro-modo-visita')?.value || 'TODAS';
    const fechaFiltro = document.getElementById('filtro-fecha-visita')?.value || '';
    const asesorFiltro = document.getElementById('filtro-asesor-visita')?.value || '';
    const clienteFiltro = document.getElementById('filtro-cliente-visita')?.value || '';
    const propiedadFiltro = document.getElementById('filtro-propiedad-visita')?.value || '';

    // Punto 3: Seguimiento histórico por cliente o por propiedad (HU-10 / RF-12)
    if (clienteFiltro || propiedadFiltro) {
      const params = new URLSearchParams();
      if (clienteFiltro) params.set('cliente', clienteFiltro);
      if (propiedadFiltro) params.set('propiedad', propiedadFiltro);
      cacheVisitas = await apiFetch(`/api/visitas?${params.toString()}`);
      renderVisitas();
      return;
    }

    // Punto 5: Visitas pendientes por fecha o por asesor (HU-08 / RF-10)
    if (modo === 'PENDIENTES' || asesorFiltro) {
      const params = new URLSearchParams();
      if (fechaFiltro) params.set('fecha', fechaFiltro);
      if (asesorFiltro) params.set('asesor', asesorFiltro);
      const qs = params.toString();
      cacheVisitas = await apiFetch(qs ? `/api/visitas/pendientes?${qs}` : '/api/visitas/pendientes');
      renderVisitas();
      return;
    }

    const url = fechaFiltro ? `/api/visitas?fecha=${encodeURIComponent(fechaFiltro)}` : '/api/visitas';
    cacheVisitas = await apiFetch(url);
    renderVisitas();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
}

window.filtrarVisitas = () => {
  const cli = document.getElementById('filtro-cliente-visita');
  const prop = document.getElementById('filtro-propiedad-visita');
  if (cli) cli.value = '';
  if (prop) prop.value = '';
  paginas.visitas = 1;
  cargarVisitas();
};

window.filtrarPorClienteOPropiedad = (origen) => {
  const cli = document.getElementById('filtro-cliente-visita');
  const prop = document.getElementById('filtro-propiedad-visita');
  const fecha = document.getElementById('filtro-fecha-visita');
  const asesor = document.getElementById('filtro-asesor-visita');
  const modo = document.getElementById('filtro-modo-visita');

  if (origen === 'cliente' && prop) prop.value = '';
  if (origen === 'propiedad' && cli) cli.value = '';
  if (fecha) fecha.value = '';
  if (asesor) asesor.value = '';
  if (modo) modo.value = 'TODAS';

  paginas.visitas = 1;
  cargarVisitas();
};

window.limpiarFiltrosVisitas = () => {
  ['filtro-fecha-visita', 'filtro-asesor-visita', 'filtro-cliente-visita', 'filtro-propiedad-visita'].forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  const modo = document.getElementById('filtro-modo-visita');
  if (modo) modo.value = 'TODAS';
  paginas.visitas = 1;
  cargarVisitas();
};

// Punto 31 / HU-05: Mostrar horarios libres sugeridos al seleccionar fecha
window.actualizarHorariosSugeridos = async () => {
  const box = document.getElementById('horarios-libres-box');
  const fecha = document.getElementById('visita-fecha')?.value;
  if (!box || !fecha) {
    if (box) box.textContent = '';
    return;
  }

  try {
    const params = new URLSearchParams({ fecha });
    const prop = document.getElementById('visita-propiedad')?.value;
    const cli = document.getElementById('visita-cliente')?.value;
    const ase = document.getElementById('visita-asesor')?.value;
    if (prop) params.set('propiedad', prop);
    if (cli) params.set('cliente', cli);
    if (ase) params.set('asesor', ase);

    const libres = await apiFetch(`/api/visitas/horarios-libres?${params.toString()}`);
    if (libres.length === 0) {
      box.innerHTML = '<strong>Horarios disponibles (HU-05):</strong> No hay horarios disponibles para la fecha seleccionada.';
    } else {
      box.innerHTML =
        '<strong>Horarios libres sugeridos (clic para elegir):</strong> ' +
        libres
          .map(
            (h) =>
              `<button type="button" class="demo-btn" onclick="document.getElementById('visita-hora').value='${escapeHtml(h)}'">${escapeHtml(h)}</button>`
          )
          .join(' ');
    }
  } catch (_) {
    box.textContent = '';
  }
};

function renderVisitas() {
  const tbody = document.getElementById('tabla-visitas');
  const items = paginar(cacheVisitas, 'visitas');
  tbody.innerHTML = '';

  actualizarSelectReprogramar();

  if (items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">No hay visitas registradas para el filtro actual.</td></tr>';
    return;
  }

  const esAdmin = usuarioSesion.rol === 'ADMINISTRADOR';

  items.forEach((v) => {
    const idSafe = Number(v.id);
    const estadoSafe = escapeHtml(v.estado);
    const acciones = [];

    if (v.estado === 'PENDIENTE') {
      acciones.push(
        `<button class="btn btn-primary btn-small" onclick="reprogramarVisita(${idSafe}, '${escapeHtml(v.fecha)}', '${escapeHtml(v.horaInicio)}')">Reprogramar</button>`
      );
      acciones.push(
        `<button class="btn btn-danger btn-small" onclick="cancelarVisita(${idSafe})">Cancelar</button>`
      );
    }

    if (esAdmin) {
      acciones.push(
        `<button class="btn btn-secondary btn-small" onclick="verHistorialVisita(${idSafe})">Historial</button>`
      );
    }

    const accionHtml =
      acciones.length > 0
        ? `<div style="display:flex; gap:4px; flex-wrap:wrap;">${acciones.join('')}</div>`
        : '<span style="color:#888; font-size:12px;">Sin acciones</span>';

    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>${idSafe}</td>
      <td>${escapeHtml(v.clienteNombre || 'Cliente #' + v.idCliente)}</td>
      <td>${escapeHtml(v.propiedadCodigo ? `${v.propiedadCodigo} (${v.propiedadDireccion})` : 'Prop #' + v.idPropiedad)}</td>
      <td>${escapeHtml(v.asesorNombre || 'Asesor #' + v.idUsuario)}</td>
      <td>${escapeHtml(v.fecha)}</td>
      <td>${escapeHtml(v.horaInicio)} - ${escapeHtml(v.horaFin)}</td>
      <td><span class="estado estado-${estadoSafe}">${estadoSafe}</span></td>
      <td>${accionHtml}</td>
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
    const selectAsesor = document.getElementById('visita-asesor');
    if (selectAsesor && selectAsesor.value) {
      body.idAsesor = Number(selectAsesor.value);
    }

    await apiFetch('/api/visitas', { method: 'POST', body: JSON.stringify(body) });
    e.target.reset();
    const box = document.getElementById('horarios-libres-box');
    if (box) box.textContent = '';
    mostrarAlerta('Visita agendada por 60 minutos (RN-04) y aviso interno generado (RN-10).');
    await cargarTodo();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
};

function actualizarSelectReprogramar() {
  const select = document.getElementById('rep-id-visita');
  if (!select || select.tagName !== 'SELECT') return;

  const valorPrevio = select.value;
  const pendientes = cacheVisitas.filter((v) => v.estado === 'PENDIENTE');

  select.innerHTML = '<option value="">-- Seleccione una visita pendiente --</option>';
  pendientes.forEach((v) => {
    const opt = document.createElement('option');
    opt.value = v.id;
    opt.textContent = `Visita #${v.id} | ${v.propiedadCodigo || 'Prop #' + v.idPropiedad} | Cliente: ${v.clienteNombre || '#' + v.idCliente} | ${v.fecha} (${v.horaInicio} - ${v.horaFin})`;
    select.appendChild(opt);
  });

  if (valorPrevio && pendientes.some((v) => String(v.id) === String(valorPrevio))) {
    select.value = valorPrevio;
  } else if (pendientes.length > 0) {
    select.value = String(pendientes[0].id);
    seleccionarVisitaParaReprogramar(pendientes[0].id);
  } else {
    const info = document.getElementById('rep-info-actual');
    if (info) {
      info.textContent = 'No tienes visitas en estado PENDIENTE actualmente. Agenda una nueva visita arriba para poder reprogramarla.';
    }
  }
}

window.seleccionarVisitaParaReprogramar = async (idVisita) => {
  const info = document.getElementById('rep-info-actual');
  const visitaObj = cacheVisitas.find((v) => Number(v.id) === Number(idVisita));
  if (!visitaObj) {
    if (info) info.textContent = 'Selecciona una visita pendiente para reprogramar.';
    return;
  }

  document.getElementById('rep-id-propiedad').value = visitaObj.idPropiedad || '';
  document.getElementById('rep-id-cliente').value = visitaObj.idCliente || '';
  document.getElementById('rep-id-asesor').value = visitaObj.idUsuario || '';
  document.getElementById('rep-fecha').value = visitaObj.fecha || '';
  document.getElementById('rep-hora').value = visitaObj.horaInicio || '10:00';

  if (info) {
    info.textContent = `Reprogramando Visita #${visitaObj.id} — Cliente: ${visitaObj.clienteNombre || '#' + visitaObj.idCliente} | Propiedad: ${visitaObj.propiedadCodigo || '#' + visitaObj.idPropiedad} | Horario actual: ${visitaObj.fecha} (${visitaObj.horaInicio} - ${visitaObj.horaFin})`;
  }

  await actualizarHorariosReprogramacion();
};

// Punto 1 / HU-06 / RF-08: Abrir formulario visual de reprogramación con sugerencia de horarios libres
window.reprogramarVisita = async (idVisita, fechaActual, horaActual) => {
  const card = document.getElementById('card-reprogramar');
  const select = document.getElementById('rep-id-visita');

  if (select) {
    select.value = String(idVisita);
  }
  await seleccionarVisitaParaReprogramar(idVisita);

  if (fechaActual) document.getElementById('rep-fecha').value = fechaActual;
  if (horaActual) document.getElementById('rep-hora').value = horaActual;
  document.getElementById('rep-motivo').value = '';

  if (card) {
    card.scrollIntoView({ behavior: 'smooth', block: 'center' });
    document.getElementById('rep-motivo').focus();
  }
};

window.cerrarFormularioReprogramar = () => {
  const motivoInput = document.getElementById('rep-motivo');
  if (motivoInput) motivoInput.value = '';
};

window.actualizarHorariosReprogramacion = async () => {
  const box = document.getElementById('rep-horarios-libres-box');
  const fecha = document.getElementById('rep-fecha')?.value;
  if (!box || !fecha) {
    if (box) box.textContent = '';
    return;
  }

  try {
    const params = new URLSearchParams({ fecha });
    const prop = document.getElementById('rep-id-propiedad')?.value;
    const cli = document.getElementById('rep-id-cliente')?.value;
    const ase = document.getElementById('rep-id-asesor')?.value;
    const idVisita = document.getElementById('rep-id-visita')?.value;

    if (prop) params.set('propiedad', prop);
    if (cli) params.set('cliente', cli);
    if (ase) params.set('asesor', ase);
    if (idVisita) params.set('visitaIgnorada', idVisita);

    const libres = await apiFetch(`/api/visitas/horarios-libres?${params.toString()}`);
    if (libres.length === 0) {
      box.innerHTML = '<strong>Horarios disponibles (HU-05 / RN-04):</strong> No hay horarios libres en esta fecha.';
    } else {
      box.innerHTML =
        '<strong>Horarios libres para reprogramar (clic para elegir):</strong> ' +
        libres
          .map(
            (h) =>
              `<button type="button" class="demo-btn" onclick="document.getElementById('rep-hora').value='${escapeHtml(h)}'">${escapeHtml(h)}</button>`
          )
          .join(' ');
    }
  } catch (_) {
    box.textContent = '';
  }
};

window.guardarReprogramacionFormulario = async (e) => {
  e.preventDefault();
  const idVisita = document.getElementById('rep-id-visita').value;
  const nuevaFecha = document.getElementById('rep-fecha').value.trim();
  const nuevaHora = document.getElementById('rep-hora').value.trim();
  const motivo = document.getElementById('rep-motivo').value.trim();

  if (!motivo) {
    mostrarAlerta('RN-08: El motivo es obligatorio para reprogramar una visita.', 'error');
    return;
  }

  try {
    await apiFetch(`/api/visitas/${idVisita}/reprogramar`, {
      method: 'PATCH',
      body: JSON.stringify({
        fecha: nuevaFecha,
        horaInicio: nuevaHora,
        motivo
      })
    });
    cerrarFormularioReprogramar();
    mostrarAlerta('Visita reprogramada exitosamente, historial registrado (RF-11) y aviso enviado (RN-10).');
    await cargarTodo();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
};

window.cancelarVisita = async (idVisita) => {
  const motivo = prompt('RN-08: Ingresa el motivo obligatorio para cancelar esta visita:');
  if (motivo === null) return;
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

// Punto 2 / HU-09 / RF-11: Consultar el historial específico de una visita
window.verHistorialVisita = async (idVisita) => {
  try {
    cacheAuditoria = await apiFetch(`/api/visitas/${idVisita}/historial`);
    paginas.auditoria = 1;
    const titulo = document.getElementById('titulo-auditoria');
    if (titulo) titulo.textContent = `Historial de Cambios de la Visita #${idVisita} (HU-09 / RF-11)`;
    const btnGlobal = document.getElementById('btn-ver-toda-auditoria');
    if (btnGlobal) btnGlobal.style.display = 'inline-block';

    renderAuditoria();
    const btnTabAuditoria = document.querySelector("button[onclick*='tab-auditoria']");
    cambiarPestana('tab-auditoria', btnTabAuditoria);
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
};

// ================= AUDITORÍA (Solo Admin - HU-09 / RF-11 / Punto 6) =================
window.cargarAuditoria = async function cargarAuditoria() {
  try {
    cacheAuditoria = await apiFetch('/api/auditor');
    const titulo = document.getElementById('titulo-auditoria');
    if (titulo) titulo.textContent = 'Registro de Auditoría e Historial de Cambios (HU-09 / RF-11)';
    const btnGlobal = document.getElementById('btn-ver-toda-auditoria');
    if (btnGlobal) btnGlobal.style.display = 'none';
    renderAuditoria();
  } catch (err) {
    mostrarAlerta(err.message, 'error');
  }
};

function renderAuditoria() {
  const tbody = document.getElementById('tabla-auditoria');
  if (!tbody) return;
  const items = paginar(cacheAuditoria, 'auditoria');
  tbody.innerHTML = '';

  if (items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">No existen movimientos registrados en el historial.</td></tr>';
    return;
  }

  items.forEach((h) => {
    const fechaFormateada = h.fechaHoraCambio ? new Date(h.fechaHoraCambio).toLocaleString() : '-';
    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>${Number(h.id)}</td>
      <td>Visita #${Number(h.idVisita)}</td>
      <td>${escapeHtml(h.usuarioNombre || 'Usuario #' + h.idUsuario)} (${escapeHtml(h.usuarioRol || '')})</td>
      <td><strong>${escapeHtml(h.tipoCambio)}</strong></td>
      <td>${escapeHtml(h.motivo)}</td>
      <td>${escapeHtml(h.fechaAnterior || '-')}</td>
      <td>${escapeHtml(h.fechaNueva || '-')}</td>
      <td>${escapeHtml(fechaFormateada)}</td>
    `;
    tbody.appendChild(fila);
  });
}

// ================= AVISOS INTERNOS (RN-10 / HU-11 / RF-13) =================
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
    const fechaFormateada = a.fechaHora ? new Date(a.fechaHora).toLocaleString() : '-';
    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>${Number(a.id)}</td>
      <td>${escapeHtml(a.usuarioNombre || 'Asesor #' + a.idUsuario)}</td>
      <td>${escapeHtml(a.mensaje)}</td>
      <td>${escapeHtml(fechaFormateada)}</td>
    `;
    tbody.appendChild(fila);
  });
}
