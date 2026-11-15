// src/middlewares/authMiddleware.js
import usuarioRepository from '../repositories/usuarioRepository.js';

// Filtro para verificar que el usuario exista y adjuntarlo a la petición
export const verificarUsuario = async (req, res, next) => {
  try {
    const usuarioId = req.headers['x-usuario-id']; // Simulación de sesión vía header
    if (!usuarioId) {
      return res.status(401).json({
        error: 'No autorizado: Falta identificación de usuario.',
        mensaje: 'No autorizado: Falta identificación de usuario.'
      });
    }

    const usuario = await usuarioRepository.findById(usuarioId);
    if (!usuario) {
      return res.status(404).json({
        error: 'Usuario no encontrado.',
        mensaje: 'Usuario no encontrado.'
      });
    }

    req.usuario = usuario; // Se inyecta en la petición para controllers y services
    next();
  } catch (error) {
    next(error);
  }
};

// Filtro específico de rol (RN-06: solo administrador)
export const requerirRol = (rolPermitido) => {
  return (req, res, next) => {
    if (req.usuario.rol !== rolPermitido) {
      return res.status(403).json({
        error: `Acceso denegado: Se requiere rol de ${rolPermitido}.`,
        mensaje: `Acceso denegado: Se requiere rol de ${rolPermitido}.`
      });
    }
    next();
  };
};