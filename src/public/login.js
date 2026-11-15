// src/public/login.js

// Si el usuario ya tiene sesión iniciada, lo redirigimos a su panel correspondiente
const sesionActual = localStorage.getItem('usuarioSesion');
if (sesionActual) {
  const usuario = JSON.parse(sesionActual);
  redirigirSegunRol(usuario.rol);
}

function redirigirSegunRol(rol) {
  if (rol === 'ADMINISTRADOR') {
    window.location.href = '/admin.html';
  } else if (rol === 'ASESOR') {
    window.location.href = '/asesor.html';
  }
}

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
      body: JSON.stringify({ usuario, password })
    });

    const data = await respuesta.json();

    if (!respuesta.ok) {
      mensajeLogin.className = 'alert error';
      mensajeLogin.textContent = data.error || 'Error al iniciar sesión.';
      return;
    }

    // Guardamos la sesión en el navegador y redirigimos según el rol
    localStorage.setItem('usuarioSesion', JSON.stringify(data));
    redirigirSegunRol(data.rol);
  } catch (error) {
    mensajeLogin.className = 'alert error';
    mensajeLogin.textContent = 'No se pudo conectar con el servidor.';
  }
});
