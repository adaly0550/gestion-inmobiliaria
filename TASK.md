# Plan de Trabajo, Validación, Pruebas y Cumplimiento (`TASK.md`)

Este documento organiza la **división equitativa del trabajo entre 2 integrantes**, el protocolo de **validación y pruebas de calidad (QA)** y la **matriz de cumplimiento** del **Sistema de Gestión Inmobiliaria**.

---

## 1. División del Trabajo entre 2 Personas

El proyecto se divide en dos frentes complementarios de igual carga técnica para que ambos integrantes dominen tanto la arquitectura como la exposición del sistema:

### 👤 Integrante 1: Arquitectura Base, Usuarios, Clientes, Propiedades y Seguridad
**Responsabilidad principal:** Configuración del motor SQLite, autenticación, control de acceso por roles (`RN-06`), módulo de Clientes (`RN-05`), catálogo de Propiedades (`RN-01`, `RN-02`) y vista de Administración.

| # | Tarea Asignada | Archivos Responsables | Estado |
| :---: | :--- | :--- | :---: |
| **1.1** | Configuración del servidor Express, conexión SQLite, `PRAGMA foreign_keys = ON`, tablas y datos semilla. | `server.js`, `src/app.js`, `src/config/database.js` | ✅ Completado |
| **1.2** | Definición de enumeraciones (`EstadoPropiedad`, `Rol`, `HorarioAtencion`) y modelos de dominio. | `src/constants/enums.js`, `src/models/*` | ✅ Completado |
| **1.3** | Implementación de login (`POST /api/auth/login`) y middlewares de sesión y rol (`verificarUsuario`, `requerirRol`). | `src/routes/authRoutes.js`, `src/middlewares/authMiddleware.js`, `src/middlewares/errorHandler.js` | ✅ Completado |
| **1.4** | Módulo completo de **Clientes** con validación obligatoria y bloqueo de identificación duplicada (`RN-05`). | `src/repositories/clienteRepository.js`, `src/services/clienteService.js`, `src/controllers/clienteController.js`, `src/routes/clienteRoutes.js` | ✅ Completado |
| **1.5** | Módulo completo de **Propiedades**: registro, listado de disponibles y cambio de estado con disparo de cancelación automática (`RN-02` y `RN-06`). | `src/repositories/propiedadRepository.js`, `src/services/propiedadService.js`, `src/controllers/propiedadController.js`, `src/routes/propiedadRoutes.js` | ✅ Completado |
| **1.6** | Interfaz de Inicio de Sesión y Panel del Administrador. | `src/public/index.html`, `src/public/login.js`, `src/public/admin.html`, `src/public/styles.css` | ✅ Completado |

---

### 👤 Integrante 2: Motor de Visitas, Horarios, Cancelaciones, Auditoría y Avisos
**Responsabilidad principal:** Lógica central de agendamiento de visitas (`RN-04`, `RN-11`), aislamiento por asesor (`RN-07`), cancelación con motivo (`RN-08`), conservación histórica (`RNF-03`), Auditoría (`HU-09`), Avisos (`RN-10`) y vista del Asesor.

| # | Tarea Asignada | Archivos Responsables | Estado |
| :---: | :--- | :--- | :---: |
| **2.1** | Repositorio de **Visitas** con consultas relacionales (`LEFT JOIN` con `Clientes`, `Propiedades` y `Usuarios`). | `src/repositories/visitaRepository.js` | ✅ Completado |
| **2.2** | Servicio de **Agendamiento (`RN-01`, `RN-04`, `RN-11`)**: cálculo de 60 minutos, validación de horario laboral y bloqueo de cruces (Asesor, Cliente, Propiedad). | `src/services/visitaService.js` (`agendarVisita`, `validarHorarioAtencion`) | ✅ Completado |
| **2.3** | Servicio de **Cancelación Manual y Automática (`RN-02`, `RN-07`, `RN-08`, `RNF-03`)**: validación de motivo obligatorio, permisos de asesor y borrado lógico. | `src/services/visitaService.js` (`cancelarVisita`, `cancelarVisitasPorPropiedad`), `src/controllers/visitaController.js`, `src/routes/visitaRoutes.js` | ✅ Completado |
| **2.4** | Módulo de **Auditoría (`HU-09`)**: persistencia en `HistorialVisitas` y endpoint exclusivo para Administrador (`/api/auditor`). | `src/repositories/historialVisitaRepository.js`, `src/app.js` | ✅ Completado |
| **2.5** | Módulo de **Avisos Internos (`RN-10`)**: generación automática al agendar/cancelar y consulta filtrada por rol (`/api/avisos`). | `src/repositories/avisoRepository.js`, `src/app.js` | ✅ Completado |
| **2.6** | Panel del Asesor y lógica dinámica de tablas, filtros por fecha y paginación en frontend. | `src/public/asesor.html`, `src/public/panel.js` | ✅ Completado |

---

## 2. Validación y Pruebas Ejecutadas (Plan de QA)

### 2.1. Validación Técnica y Sintáctica
- **Verificación de sintaxis ES Modules**: Todos los archivos `.js` del backend y frontend fueron validados sin errores de sintaxis.
- **Integridad Referencial SQLite**: Verificada la activación de `PRAGMA foreign_keys = ON` y las llaves foráneas en `Visitas`, `HistorialVisitas` y `Avisos`.

### 2.2. Matriz de Pruebas Funcionales y de Reglas de Negocio (24/24 Superadas)

| ID Prueba | Caso de Prueba | Resultado Esperado | Resultado Obtenido | Estado |
| :---: | :--- | :--- | :--- | :---: |
| **QA-01** | Carga de interfaz web (`index.html`, `admin.html`, `asesor.html`) | Respuesta HTTP `200 OK` | `200 OK` en local y túnel ngrok | ✅ PASS |
| **QA-02** | Login con credenciales incorrectas | HTTP `401` con mensaje de error | `401 Unauthorized` | ✅ PASS |
| **QA-03** | Login con `admin`, `carlos` y `laura` | HTTP `200` devolviendo `id`, `nombres`, `usuario`, `rol` | `200 OK` con rol exacto | ✅ PASS |
| **QA-04** | Petición a ruta protegida sin `x-usuario-id` | HTTP `401 No autorizado` | `401 Unauthorized` | ✅ PASS |
| **QA-05** | Registro de cliente sin identificación o teléfono (`RN-05`) | HTTP `400` indicando campos obligatorios | `400 Bad Request` (`RN-05`) | ✅ PASS |
| **QA-06** | Registro de cliente con identificación repetida (`RN-05`) | HTTP `400` indicando duplicado | `400 Bad Request` | ✅ PASS |
| **QA-07** | Asesor intenta crear propiedad o cambiar estado (`RN-06`) | HTTP `403 Acceso denegado` | `403 Forbidden` | ✅ PASS |
| **QA-08** | Asesor intenta consultar `/api/auditor` (`RN-06` / `HU-09`) | HTTP `403 Acceso denegado` | `403 Forbidden` | ✅ PASS |
| **QA-09** | Admin consulta `/api/auditor` (`HU-09`) | HTTP `200` con lista de historial | `200 OK` | ✅ PASS |
| **QA-10** | Agendar visita en fecha pasada (`RN-11`) | HTTP `400` bloqueando fecha pasada | `400 Bad Request` | ✅ PASS |
| **QA-11** | Agendar visita en día domingo (`RN-11`) | HTTP `400` indicando domingo cerrado | `400 Bad Request` | ✅ PASS |
| **QA-12** | Agendar visita sábado fuera de `09:00-13:00` (`RN-11`) | HTTP `400` indicando horario de sábado | `400 Bad Request` | ✅ PASS |
| **QA-13** | Agendar visita lunes-viernes fuera de `09:00-18:00` (`RN-11`) | HTTP `400` indicando horario L-V | `400 Bad Request` | ✅ PASS |
| **QA-14** | Agendar visita válida (`RN-01`, `RN-04`, `RN-10`) | Crea visita de 60 min (`PENDIENTE`) + genera aviso | `201 Created` (`horaFin = +60m`) | ✅ PASS |
| **QA-15** | Cruce de horario para mismo asesor, cliente o propiedad (`RN-04`) | HTTP `400` rechazando solapamiento | `400 Bad Request` (`RN-04`) | ✅ PASS |
| **QA-16** | Cancelar visita sin ingresar motivo (`RN-08`) | HTTP `400` exigiendo motivo obligatorio | `400 Bad Request` (`RN-08`) | ✅ PASS |
| **QA-17** | Cancelar visita válida (`RNF-03`, `HU-09`, `RN-10`) | Estado pasa a `CANCELADA`, no se borra fila, crea auditoría y aviso | `200 OK` y trazabilidad completa | ✅ PASS |
| **QA-18** | Cambio de propiedad a `RESERVADA`/`VENDIDA`/`ALQUILADA` (`RN-02`) | Cancela automáticamente visitas `PENDIENTE` de esa propiedad | `200 OK` + cancelación en cascada | ✅ PASS |

---

## 3. Matriz de Cumplimiento del Proyecto

| Categoría | Requisito / Regla | Nivel de Cumplimiento | Evidencia en el Proyecto |
| :--- | :--- | :---: | :--- |
| **Base de Datos** | SQLite con relaciones `FOREIGN KEY` (`RNF-01`) | **100%** | `src/config/database.js` (`inmobiliaria.sqlite`) |
| **Regla de Negocio** | `RN-01`: Solo propiedades `DISPONIBLE` reciben visitas | **100%** | `src/services/visitaService.js` |
| **Regla de Negocio** | `RN-02`: Cancelación automática al cambiar estado de propiedad | **100%** | `src/services/propiedadService.js` y `visitaService.js` |
| **Regla de Negocio** | `RN-04`: Duración exacta de 60 min y cero cruces de horario | **100%** | `src/services/visitaService.js` |
| **Regla de Negocio** | `RN-05`: Campos obligatorios y cédula/RUC único en Clientes | **100%** | `src/services/clienteService.js` |
| **Regla de Negocio** | `RN-06`: Exclusividad de Administrador (Propiedades y Auditoría) | **100%** | `src/routes/propiedadRoutes.js` y `src/app.js` |
| **Regla de Negocio** | `RN-07`: Asesor solo gestiona y visualiza sus propias visitas | **100%** | `src/services/visitaService.js` |
| **Regla de Negocio** | `RN-08`: Motivo obligatorio para cancelar visitas | **100%** | `src/services/visitaService.js` y `src/public/panel.js` |
| **Regla de Negocio** | `RN-10`: Generación automática de Avisos Internos | **100%** | `src/repositories/avisoRepository.js` y `visitaService.js` |
| **Regla de Negocio** | `RN-11`: Validación de horario real de atención (L-V y Sábados) | **100%** | `src/constants/enums.js` y `visitaService.js` |
| **No Funcional** | `RNF-03`: Prohibido borrar visitas (`CANCELADA` + `HistorialVisitas`) | **100%** | `src/repositories/visitaRepository.js` y `historialVisitaRepository.js` |
| **Entorno / IDE** | Ejecución directa en **IntelliJ IDEA** (Botón Run ▶) y consola | **100%** | `src/IniciarServidor.java` y `.idea/runConfigurations/` |
