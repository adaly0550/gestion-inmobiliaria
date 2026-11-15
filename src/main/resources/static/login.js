// src/main/resources/static/login.js - Inicio de sesión con cookie de sesión HTTP (Punto 8)

function redirigirSegunRol(rol) {
  if (rol === 'ADMINISTRADOR') {
    window.location.href = '/admin.html';
  } else if (rol === 'ASESOR') {
    window.location.href = '/asesor.html';
  }
}

// Verificar si ya existe una sesión activa en el servidor
(async function verificarSesionActiva() {
  try {
    const res = await fetch('/api/auth/me', { credentials: 'same-origin' });
    if (res.ok) {
      const usuario = await res.json();
      localStorage.setItem('usuarioSesion', JSON.stringify(usuario));
      redirigirSegunRol(usuario.rol);
    } else {
      localStorage.removeItem('usuarioSesion');
    }
  } catch (_) {
    // Sin sesión activa
  }
})();

// Botón rápido para rellenar credenciales de prueba
window.llenarLogin = (usuario, password) => {
  document.getElementById('usuario').value = usuario;
  document.getElementById('password').value = password;
};

const formLogin = document.getElementById('form-login');
const mensajeLogin = document.getElementById('mensaje-login');

formLogin.addEventListener('submit', async (e) => {
  e.preventDefault();
  mensajeLogin.className = 'alert';
  mensajeLogin.textContent = '';

  const usuario = document.getElementById('usuario').value.trim();
  const password = document.getElementById('password').value;

  try {
    const respuesta = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',
      body: JSON.stringify({ usuario, password })
    });

    const data = await respuesta.json();

    if (!respuesta.ok) {
      mensajeLogin.className = 'alert error';
      mensajeLogin.textContent = data.error || data.mensaje || 'Error al iniciar sesión.';
      return;
    }

    localStorage.setItem('usuarioSesion', JSON.stringify(data));
    redirigirSegunRol(data.rol);
  } catch (error) {
    mensajeLogin.className = 'alert error';
    mensajeLogin.textContent = 'No se pudo conectar con el servidor.';
  }
});
