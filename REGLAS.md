# Reglas del Proyecto (`REGLAS.md`)

Este documento establece los **principios arquitectónicos, convenciones de código y restricciones técnicas/operativas** que rigen el desarrollo y mantenimiento del **Sistema de Gestión Inmobiliaria**.

---

## 1. Principios del Proyecto

### 1.1. Arquitectura en Capas (Separación de Responsabilidades)
El sistema está estructurado bajo una arquitectura limpia en capas dentro de `src/` para garantizar mantenibilidad, legibilidad y facilidad de prueba:
- **Configuración (`src/config/`)**: Inicialización de la conexión SQLite, activación de integridad referencial (`PRAGMA foreign_keys = ON`) y carga de datos semilla (`database.js`).
- **Constantes y Enumeraciones (`src/constants/`)**: Centralización de estados (`EstadoPropiedad`, `EstadoVisita`), roles (`Rol`), tipos de cambio (`TipoCambio`) y horarios de atención (`HorarioAtencion`) usando objetos inmutables (`Object.freeze`).
- **Modelos (`src/models/`)**: Representación clara de las entidades del dominio (`Usuario`, `Propiedad`, `Cliente`, `Visita`, `HistorialVisita`) y sus llaves foráneas.
- **Repositorios (`src/repositories/`)**: Única capa autorizada para ejecutar consultas SQL (`SELECT`, `INSERT`, `UPDATE`) sobre SQLite. Ningún controlador o servicio debe escribir SQL directamente.
- **Servicios (`src/services/`)**: Contienen el **100% de la lógica y reglas de negocio** (`RN-01` a `RN-11`). Validan datos antes de invocar a los repositorios.
- **Controladores (`src/controllers/`)**: Gestionan exclusivamente la entrada/salida HTTP (`req`, `res`), códigos de estado (`200`, `201`, `400`, `401`, `403`, `404`, `500`) y delegan el procesamiento a los servicios.
- **Middlewares (`src/middlewares/`)**: Validación transversal de sesión (`verificarUsuario`), autorización por rol (`requerirRol`) y manejo centralizado de errores (`errorHandler`).
- **Presentación (`src/public/`)**: Interfaz web ligera en HTML5, CSS3 y JavaScript Vanilla (`index.html`, `admin.html`, `asesor.html`, `login.js`, `panel.js`).

### 1.2. Integridad y Trazabilidad Total (Auditoría por Diseño)
- Toda operación crítica sobre el agendamiento o cancelación de visitas debe dejar rastro verificable en las tablas de **Auditoría (`HistorialVisitas`)** y **Avisos Internos (`Avisos`)**.
- Se prioriza la preservación histórica de los datos por encima de la eliminación física.

### 1.3. Simplicidad y Cero Dependencias Innecesarias
- Uso de **Node.js con ES Modules (`"type": "module"`)**, **Express** y **SQLite** (`sqlite` + `sqlite3`).
- El frontend utiliza JavaScript nativo (`fetch` API) sin frameworks pesados para asegurar carga inmediata y compatibilidad total con cualquier navegador o entorno académico (IntelliJ IDEA / VS Code).

### 1.4. Consistencia en Contratos de API
- Todas las respuestas de error del backend deben retornar un objeto JSON uniforme con las llaves `{ error, mensaje }` y el código HTTP correspondiente.

---

## 2. Restricciones del Proyecto

### 2.1. Restricciones de Base de Datos y Persistencia
1. **Prohibición de Borrado Físico (`RNF-03`)**:
   - Queda estrictamente prohibido ejecutar sentencias `DELETE FROM Visitas` o `DELETE FROM HistorialVisitas`.
   - Toda cancelación se realiza mediante **borrado lógico** (`UPDATE Visitas SET estado = 'CANCELADA'`).
2. **Integridad Referencial Obligatoria**:
   - La base de datos debe operar siempre con `PRAGMA foreign_keys = ON;`.
   - No se pueden registrar visitas hacia clientes, propiedades o usuarios inexistentes.
3. **Unicidad de Registros**:
   - No se permiten dos clientes con la misma `identificacion` (controlado en servicio y mediante índice único `idx_clientes_identificacion`).
   - No se permiten dos propiedades con el mismo `codigoInterno` (`UNIQUE`).
   - No se permiten dos usuarios con el mismo nombre de `usuario` (`UNIQUE`).

### 2.2. Restricciones de Seguridad y Control de Acceso
1. **Autenticación Obligatoria en Operaciones del Sistema**:
   - Los endpoints de propiedades, visitas, avisos y auditoría exigen el encabezado `x-usuario-id` validado contra la tabla `Usuarios`.
2. **Segregación Estricta de Roles (`RN-06` y `RN-07`)**:
   - **Rol `ADMINISTRADOR`**: Único autorizado para registrar propiedades, cambiar el estado de una propiedad (`DISPONIBLE`, `RESERVADA`, `VENDIDA`, `ALQUILADA`) y consultar el módulo de auditoría (`/api/auditor`).
   - **Rol `ASESOR`**: Solo puede consultar propiedades en estado `DISPONIBLE`, gestionar clientes y visualizar/cancelar únicamente las visitas que él mismo agendó (`idUsuario === usuarioSesion.id`).

### 2.3. Restricciones Operativas y de Negocio
1. **Duración Fija de Visitas (`RN-04`)**:
   - El usuario solo ingresa la `horaInicio`; el sistema calcula automáticamente `horaFin = horaInicio + 60 minutos`. No se admiten duraciones manuales distintas a 1 hora.
2. **Cero Solapamiento de Horarios (`RN-04`)**:
   - Ningún asesor, ningún cliente y ninguna propiedad pueden tener dos visitas activas (`PENDIENTE`) que se crucen en la misma fecha y rango horario.
3. **Restricción de Horario Laboral (`RN-11`)**:
   - **Lunes a Viernes**: `09:00` a `18:00` (última visita inicia a las `17:00`).
   - **Sábados**: `09:00` a `13:00` (última visita inicia a las `12:00`).
   - **Domingos y fechas/horas pasadas**: Estrictamente bloqueados para agendamiento.

### 2.4. Restricciones de Control de Versiones (Git)
- No se deben subir al repositorio las carpetas `node_modules/`, `out/` ni archivos de variables de entorno `.env` (definido en `.gitignore`).
