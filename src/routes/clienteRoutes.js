import { Router } from 'express';
import * as clienteController from '../controllers/clienteController.js';
import { verificarUsuario } from '../middlewares/authMiddleware.js';

const router = Router();

// Si se envía el header de usuario se verifica, si no, se permite continuar
const authOpcional = (req, res, next) => {
  if (req.headers['x-usuario-id']) {
    return verificarUsuario(req, res, next);
  }
  next();
};

router.use(authOpcional);

// 1. Ruta para obtener todos los clientes (GET /api/clientes)
router.get('/', clienteController.obtenerClientes);

// 2. Ruta para obtener un solo cliente por su ID (GET /api/clientes/:id)
router.get('/:id', clienteController.obtenerClientePorId);

// 3. Ruta para crear un nuevo cliente (POST /api/clientes)
router.post('/', clienteController.crearCliente);

export default router;
