// src/middlewares/errorHandler.js
export const errorHandler = (err, req, res, next) => {
  console.error('[Error Handler]:', err);
  const textoError = err.message || 'Error interno del servidor.';
  return res.status(err.status || 500).json({
    error: textoError,
    mensaje: textoError
  });
};

export default errorHandler;
