// src/app.js
import express from 'express';
import path from 'path';
import { fileURLToPath } from 'url';
import authRoutes from './routes/authRoutes.js';
import clienteRoutes from './routes/clienteRoutes.js';
import propiedadRoutes from './routes/propiedadRoutes.js';
import visitaRoutes from './routes/visitaRoutes.js';
import historialVisitaRepository from './repositories/historialVisitaRepository.js';
import avisoRepository from './repositories/avisoRepository.js';
import { verificarUsuario } from './middlewares/authMiddleware.js';
import { errorHandler } from './middlewares/errorHandler.js';
import { Rol } from './constants/enums.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();

// Permite leer datos en formato JSON en las peticiones (req.body)
app.use(express.json());

// Servir la interfaz web visual (HTML, JS, CSS) para ver en el navegador
app.use(express.static(path.join(__dirname, 'public')));

// Rutas principales del sistema
app.use('/api/auth', authRoutes);
app.use('/api/clientes', clienteRoutes);
app.use('/api/propiedades', propiedadRoutes);
app.use('/api/visitas', visitaRoutes);

// Ruta del Auditor (HU-09 / RN-06: solo Administrador)
app.get('/api/auditor', verificarUsuario, async (req, res, next) => {
  try {
    if (req.usuario.rol !== Rol.ADMINISTRADOR) {
      return res.status(403).json({ error: 'Acceso denegado: Solo el administrador puede auditar.' });
    }
    const historial = await historialVisitaRepository.findAll();
    return res.status(200).json(historial);
  } catch (error) {
    next(error);
  }
});

// Ruta de Avisos Internos (RN-10)
app.get('/api/avisos', verificarUsuario, async (req, res, next) => {
  try {
    if (req.usuario.rol === Rol.ADMINISTRADOR) {
      const avisos = await avisoRepository.findAll();
      return res.status(200).json(avisos);
    }
    const avisos = await avisoRepository.findByUsuarioId(req.usuario.id);
    return res.status(200).json(avisos);
  } catch (error) {
    next(error);
  }
});

// Manejador de errores
app.use(errorHandler);

export default app;
