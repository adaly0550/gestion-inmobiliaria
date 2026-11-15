// src/routes/visitaRoutes.js
import { Router } from 'express';
import visitaController from '../controllers/visitaController.js';
import { verificarUsuario } from '../middlewares/authMiddleware.js';

const router = Router();

// Todas las rutas de visitas exigen autenticación previa
router.use(verificarUsuario);

router.post('/', (req, res) => visitaController.agendar(req, res));
router.patch('/:id/cancelar', (req, res) => visitaController.cancelar(req, res));
router.get('/pendientes', (req, res) => visitaController.listarPendientes(req, res));
router.get('/', (req, res) => visitaController.listarTodas(req, res));

export default router;
