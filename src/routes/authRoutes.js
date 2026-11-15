// src/routes/authRoutes.js
import { Router } from 'express';
import usuarioRepository from '../repositories/usuarioRepository.js';

const router = Router();

// Login único del sistema: valida credenciales y devuelve el rol para redirigir
router.post('/login', async (req, res) => {
  try {
    const { usuario, password } = req.body;

    if (!usuario || !password) {
      return res.status(400).json({ error: 'Debe ingresar usuario y contraseña.' });
    }

    const encontrado = await usuarioRepository.findByUsuario(usuario.trim());
    if (!encontrado || encontrado.password !== password) {
      return res.status(401).json({ error: 'Usuario o contraseña incorrectos.' });
    }

    return res.status(200).json({
      id: encontrado.id,
      nombres: encontrado.nombres,
      usuario: encontrado.usuario,
      rol: encontrado.rol
    });
  } catch (error) {
    return res.status(500).json({ error: 'Error al iniciar sesión.' });
  }
});

export default router;
