// src/routes/propiedadRoutes.js
import { Router } from 'express';
import propiedadController from '../controllers/propiedadController.js';
import { verificarUsuario, requerirRol } from '../middlewares/authMiddleware.js';
import { Rol } from '../constants/enums.js';

const router = Router();

router.use(verificarUsuario);

router.get('/disponibles', (req, res) => propiedadController.listarDisponibles(req, res));
router.get('/', (req, res) => propiedadController.listarTodas(req, res));

// RN-06: Solo Administrador puede registrar o cambiar el estado de propiedades
router.post('/', requerirRol(Rol.ADMINISTRADOR), (req, res) => propiedadController.registrar(req, res));
router.patch('/:id/estado', requerirRol(Rol.ADMINISTRADOR), (req, res) => propiedadController.cambiarEstado(req, res));

export default router;
