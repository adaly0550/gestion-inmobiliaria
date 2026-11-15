# Registro de Revisión y Cambios (`cambios.md`)

Este documento detalla la revisión técnica realizada sobre el proyecto **Gestión Inmobiliaria**, asegurando que **no existan cambios bruscos**, que **el flujo y estilo original del código se mantengan intactos**, y que **todas las reglas de negocio sigan funcionando exactamente igual** para facilitar su lectura y exposición.

---

## 1. Revisión de Sintaxis y Módulos (`ES Modules`)

- Se verificaron todos los archivos `.js` del proyecto (`server.js`, `src/app.js`, `config`, `constants`, `controllers`, `middlewares`, `models`, `repositories`, `routes`, `services` y `public`) con `node --check`.
- **Corrección en `src/models/index.js` y modelos vacíos**:
  - **Problema detectado**: Los archivos `Cliente.js`, `Propiedad.js`, `Usuario.js`, `Visita.js` e `HistorialVisita.js` dentro de `src/models/` estaban en blanco (`0 bytes`), y `src/models/index.js` contenía un fragmento antiguo de servidor Express con una importación rota (`./routes/clienteRoutes.js`, ruta inexistente desde `src/models/`).
  - **Solución aplicada**: Se definieron clases sencillas y legibles en cada archivo de `src/models/` reflejando exactamente las columnas y llaves foráneas de cada tabla, y se actualizó `src/models/index.js` para exportar todos los modelos limpiamente.

---

## 2. Corrección de Relaciones entre Tablas y Consultas (`SQLite`)

- **Activación de Integridad Referencial (`src/config/database.js`)**:
  - Se agregó `PRAGMA foreign_keys = ON;` al abrir la conexión SQLite para que el motor valide las relaciones entre tablas.
- **Declaración explícita de Claves Foráneas (`FOREIGN KEY`) en `src/config/database.js` e `inmobiliaria.sqlite`**:
  - **Tabla `Visitas`**:
    - `FOREIGN KEY (idCliente) REFERENCES Clientes(id)`
    - `FOREIGN KEY (idPropiedad) REFERENCES Propiedades(id)`
    - `FOREIGN KEY (idUsuario) REFERENCES Usuarios(id)`
  - **Tabla `HistorialVisitas`**:
    - `FOREIGN KEY (idVisita) REFERENCES Visitas(id)`
    - `FOREIGN KEY (idUsuario) REFERENCES Usuarios(id)`
  - **Tabla `Avisos`**:
    - `FOREIGN KEY (idUsuario) REFERENCES Usuarios(id)`
- **Relaciones en Consultas (`JOIN`s) en Repositorios**:
  - En `src/repositories/visitaRepository.js`: El método `findById(id)` ahora incluye los `LEFT JOIN` con `Clientes`, `Propiedades` y `Usuarios` (igual que `findAll` y `findByEstado`), devolviendo `clienteNombre`, `propiedadCodigo`, `propiedadDireccion` y `asesorNombre`.
  - En `src/repositories/historialVisitaRepository.js`: Los métodos `findByVisitaId` y `findAll` ahora incluyen los `LEFT JOIN` con `Usuarios`, `Visitas` y `Propiedades` para exponer los datos relacionados en la auditoría.
- **Trazabilidad de Usuario en Cancelación Automática (`RN-02`)**:
  - En `src/services/propiedadService.js` y `src/services/visitaService.js`: Al cambiar el estado de una propiedad (`cambiarEstado`), se pasa el `usuario` que realizó la acción hacia `cancelarVisitasPorPropiedad(id, motivo, usuario)` para que el historial de auditoría (`HistorialVisitas`) registre al responsable del cambio, manteniendo el aviso interno (`Avisos`) dirigido al asesor asignado a la visita (`v.idUsuario`).

---

## 3. Consistencia en Respuestas de Middleware

- En `src/middlewares/authMiddleware.js` y `src/middlewares/errorHandler.js`:
  - Se incluyeron ambas propiedades `{ error, mensaje }` en las respuestas JSON de error para mantener total uniformidad con los controladores y con el cliente web (`panel.js` y `app.js`).

---

## 4. Configuración de Git (`.gitignore`) y Adaptación para IntelliJ IDEA

- Se agregó el archivo `.gitignore` para excluir `node_modules/`, `.env` y `out/` del repositorio en GitHub.
- Se agregaron los archivos de configuración del entorno **IntelliJ IDEA** (`.idea/`, `gestion-inmobiliaria.iml` y `src/IniciarServidor.java`) además de una copia lista en `gestion-inmobiliaria-intellij/` para poder abrir y ejecutar el proyecto directamente desde **IntelliJ IDEA (Community o Ultimate)** usando el botón verde **Run (▶)** sin alterar ninguna regla ni archivo de negocio.

---

## 5. Resumen de Reglas de Negocio Verificadas (Guía rápida para exposición)

| Código | Regla de Negocio / Requisito | Ubicación en el Código |
| :--- | :--- | :--- |
| **RN-01** | Solo propiedades en estado `DISPONIBLE` pueden recibir visitas. | `src/services/visitaService.js` (`agendarVisita`) |
| **RN-02 / RF-09** | Si una propiedad pasa a `RESERVADA`, `VENDIDA` o `ALQUILADA`, sus visitas `PENDIENTE` se cancelan automáticamente. | `src/services/propiedadService.js` (`cambiarEstado`) y `src/services/visitaService.js` (`cancelarVisitasPorPropiedad`) |
| **RN-04** | Cada visita dura exactamente **60 minutos** y valida que no existan cruces de horario para el asesor, el cliente ni la propiedad. | `src/services/visitaService.js` (`agendarVisita`) |
| **RN-05** | Para registrar un cliente, `identificacion` (única), `nombre` y `telefono` son obligatorios. | `src/services/clienteService.js` (`crearCliente`) |
| **RN-06 / HU-09** | Solo el `ADMINISTRADOR` puede registrar/cambiar estado de propiedades y consultar el módulo de Auditoría (`/api/auditor`). | `src/routes/propiedadRoutes.js` y `src/app.js` |
| **RN-07** | El `ASESOR` solo visualiza y puede cancelar sus propias visitas asignadas. | `src/services/visitaService.js` (`cancelarVisita`, `listarPendientes`, `listarTodas`) |
| **RN-08 / HU-06** | Es obligatorio ingresar un motivo para poder cancelar una visita. | `src/services/visitaService.js` (`cancelarVisita`) |
| **RN-10** | El sistema genera avisos internos automáticos al agendar o cancelar una visita. | `src/services/visitaService.js` (`generarAvisoInterno`) y `src/repositories/avisoRepository.js` |
| **RN-11** | Validación de horario de atención (Lunes a Viernes `09:00-18:00`, Sábados `09:00-13:00`, Domingos cerrado, sin fechas pasadas). | `src/services/visitaService.js` (`validarHorarioAtencion`) |
| **RNF-03** | Ninguna visita se elimina físicamente de la base de datos (`UPDATE estado = 'CANCELADA'`) y todo cambio queda en `HistorialVisitas`. | `src/repositories/visitaRepository.js` y `src/repositories/historialVisitaRepository.js` |
