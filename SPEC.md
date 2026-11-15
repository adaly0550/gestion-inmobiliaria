# Especificación del Sistema (`SPEC.md`)

Este documento detalla la especificación técnica y funcional del **Sistema de Gestión Inmobiliaria**, incluyendo los **Requisitos Funcionales (RF)**, **Requisitos No Funcionales (RNF)**, **Historias de Usuario (HU)**, **Reglas de Negocio (RN)** y **Criterios de Aceptación**.

---

## 1. Roles del Sistema

| Rol | Descripción y Alcance | Credenciales de Prueba |
| :--- | :--- | :--- |
| **ADMINISTRADOR** | Control total del catálogo de propiedades (registro y cambio de estado), clientes, todas las visitas del sistema, avisos globales y módulo exclusivo de Auditoría. | `admin` / `admin123` |
| **ASESOR** | Consulta de propiedades disponibles, registro/consulta de clientes, agendamiento y cancelación exclusiva de sus propias visitas y consulta de sus avisos internos. | `carlos` / `carlos123`<br>`laura` / `laura123` |

---

## 2. Requisitos Funcionales (RF) e Historias de Usuario (HU)

| Código | Historia / Requisito | Descripción Funcional | Endpoint / Componente |
| :--- | :--- | :--- | :--- |
| **RF-01 / HU-01** | Autenticación por Rol | Permitir el inicio de sesión validando usuario y contraseña, redirigiendo al panel de Administrador o Asesor según su rol. | `POST /api/auth/login` |
| **RF-02 / HU-02** | Gestión de Clientes | Registrar nuevos clientes validando campos obligatorios e identificación única, y listar todos los clientes registrados. | `GET /api/clientes`<br>`GET /api/clientes/:id`<br>`POST /api/clientes` |
| **RF-03 / HU-03** | Registro de Propiedades | Permitir al Administrador dar de alta propiedades indicando código interno único, dirección, tipo (`Casa`/`Departamento`) y modalidad (`VENTA`/`ALQUILER`). | `POST /api/propiedades` |
| **RF-04 / HU-04** | Consulta de Propiedades | Permitir listar todas las propiedades (vista Admin) o filtrar únicamente las propiedades en estado `DISPONIBLE` (vista Asesor y selector de visitas). | `GET /api/propiedades`<br>`GET /api/propiedades/disponibles` |
| **RF-05 / HU-05** | Agendamiento de Visitas | Programar una visita asociando un cliente, una propiedad disponible, el asesor en sesión, fecha y hora de inicio. | `POST /api/visitas` |
| **RF-06** | Cálculo Automático de Duración | Calcular de forma automática la hora de finalización de la visita sumando exactamente **60 minutos** a la hora de inicio. | `visitaService.agendarVisita` |
| **RF-07** | Validación de Cruces y Horarios | Impedir el agendamiento si existe cruce de horario para el asesor, el cliente o la propiedad, o si está fuera del horario de atención. | `visitaService.agendarVisita`<br>`validarHorarioAtencion` |
| **RF-08 / HU-06** | Cancelación Manual de Visita | Permitir cancelar una visita en estado `PENDIENTE` exigiendo un motivo obligatorio, conservando el registro y guardando el cambio en auditoría. | `PATCH /api/visitas/:id/cancelar` |
| **RF-09 / HU-07** | Cambio de Estado y Cancelación Automática | Al cambiar una propiedad a `RESERVADA`, `VENDIDA` o `ALQUILADA`, cancelar automáticamente todas sus visitas `PENDIENTE`, registrando auditoría y aviso. | `PATCH /api/propiedades/:id/estado` |
| **RF-10 / HU-08** | Bandeja de Avisos Internos | Generar y consultar notificaciones internas automáticas cada vez que se agenda o cancela una visita (manual o automáticamente). | `GET /api/avisos` |
| **RF-11 / HU-09** | Consulta de Auditoría | Permitir exclusivamente al Administrador consultar el historial completo de cancelaciones con responsable, motivo, visita y fecha/hora exacta. | `GET /api/auditor` |

---

## 3. Reglas de Negocio (RN)

| Código | Nombre de la Regla | Especificación Detallada | Implementación |
| :--- | :--- | :--- | :--- |
| **RN-01** | Disponibilidad para Visitas | Solo las propiedades cuyo estado sea `DISPONIBLE` pueden recibir visitas agendadas. Si está en `RESERVADA`, `VENDIDA` o `ALQUILADA`, se rechaza la solicitud. | `src/services/visitaService.js` |
| **RN-02** | Cancelación en Cascada por Estado de Propiedad | Cuando una propiedad cambia de `DISPONIBLE` a cualquier otro estado (`RESERVADA`, `VENDIDA`, `ALQUILADA`), todas sus visitas en estado `PENDIENTE` pasan automáticamente a `CANCELADA`, registrando el motivo automático en `HistorialVisitas` y enviando un aviso al asesor asignado. | `src/services/propiedadService.js`<br>`src/services/visitaService.js` |
| **RN-04** | Duración de 60 Minutos y No Solapamiento | Toda visita tiene una duración exacta de **60 minutos** (`horaFin = horaInicio + 60 min`). En una misma fecha, no puede existir solapamiento (`horaInicio < v.horaFin && horaFin > v.horaInicio`) para:<br>1. El mismo **Asesor**.<br>2. El mismo **Cliente** (no puede estar en dos propiedades a la vez).<br>3. La misma **Propiedad**. | `src/services/visitaService.js` |
| **RN-05** | Datos Obligatorios y Unicidad de Cliente | Para registrar un cliente son obligatorios: `identificacion`, `nombre` y `telefono`. La `identificacion` no puede repetirse en la base de datos. | `src/services/clienteService.js`<br>`src/config/database.js` |
| **RN-06** | Exclusividad del Rol Administrador | Únicamente los usuarios con rol `ADMINISTRADOR` pueden:<br>- Registrar propiedades (`POST /api/propiedades`).<br>- Cambiar el estado de propiedades (`PATCH /api/propiedades/:id/estado`).<br>- Acceder al módulo de Auditoría (`GET /api/auditor`). | `src/routes/propiedadRoutes.js`<br>`src/app.js` |
| **RN-07** | Aislamiento por Asesor | Un usuario con rol `ASESOR` solo puede visualizar sus propias visitas (`idUsuario === usuarioSesion.id`) y solo puede cancelar las visitas que le pertenecen. | `src/services/visitaService.js` |
| **RN-08** | Motivo de Cancelación Obligatorio | Ninguna visita puede cancelarse sin proporcionar un texto de `motivo` válido (no vacío ni solo espacios). | `src/services/visitaService.js` |
| **RN-10** | Avisos Internos Automáticos | El sistema registra automáticamente un aviso en la tabla `Avisos` asociado al asesor responsable cuando se agenda una visita o cuando se cancela (manual o automáticamente). | `src/services/visitaService.js`<br>`src/repositories/avisoRepository.js` |
| **RN-11** | Horario de Atención Inmobiliaria | Las visitas solo pueden agendarse en fechas/horas futuras y dentro del horario laboral:<br>- **Lunes a Viernes**: `09:00` a `18:00` (última cita inicia `17:00`).<br>- **Sábados**: `09:00` a `13:00` (última cita inicia `12:00`).<br>- **Domingos**: Cerrado. | `src/constants/enums.js`<br>`src/services/visitaService.js` |

---

## 4. Requisitos No Funcionales (RNF)

- **RNF-01 (Persistencia Relacional)**: Uso de base de datos relacional **SQLite** (`inmobiliaria.sqlite`) con llaves foráneas activas (`PRAGMA foreign_keys = ON`) relacionando `Usuarios`, `Propiedades`, `Clientes`, `Visitas`, `HistorialVisitas` y `Avisos`.
- **RNF-02 (Usabilidad y Paginación)**: Interfaz web responsiva con pestañas organizadas por rol, alertas claras de éxito/error y paginación de tablas (5 registros por página).
- **RNF-03 (Conservación e Inmutabilidad Histórica)**: Ninguna visita se elimina físicamente de la base de datos. Las visitas canceladas permanecen con estado `CANCELADA` y su trazabilidad queda almacenada permanentemente en `HistorialVisitas`.

---

## 5. Criterios de Aceptación (Escenarios Gherkin / Dado-Cuando-Entonces)

### CA-01: Registro de Cliente y Validación de Duplicados (`RN-05`)
- **Dado** que un usuario intenta registrar un cliente en `/api/clientes`,
- **Cuando** omite `identificacion`, `nombre` o `telefono`, **o** ingresa una `identificacion` ya existente,
- **Entonces** el sistema rechaza el registro con código `400` y muestra el mensaje de validación correspondiente sin duplicar filas.

### CA-02: Agendamiento Válido y Duración de 60 Minutos (`RN-01`, `RN-04`, `RN-10`, `RN-11`)
- **Dado** un cliente registrado y una propiedad en estado `DISPONIBLE`,
- **Cuando** un usuario agenda una visita para un día laborable futuro a las `10:00` sin cruces de horario,
- **Entonces** el sistema crea la visita en estado `PENDIENTE` con `horaInicio = "10:00"` y `horaFin = "11:00"`, retorna `201 Created` y genera un aviso interno para el asesor.

### CA-03: Bloqueo de Cruce de Horarios (`RN-04`)
- **Dado** que existe una visita `PENDIENTE` de `10:00` a `11:00` en una fecha determinada,
- **Cuando** se intenta agendar otra visita el mismo día a las `10:30` con el mismo asesor, el mismo cliente o la misma propiedad,
- **Entonces** el sistema rechaza la petición con estado `400` indicando el conflicto de horario (`RN-04`).

### CA-04: Bloqueo por Horario de Atención (`RN-11`)
- **Dado** el formulario de agendamiento de visitas,
- **Cuando** se selecciona una fecha pasada, un día domingo, un sábado después de las `12:00` o un día de lunes a viernes después de las `17:00`,
- **Entonces** el sistema impide el registro con estado `400` explicando el horario de atención vigente.

### CA-05: Cancelación Manual y Trazabilidad (`RN-07`, `RN-08`, `RNF-03`, `HU-09`)
- **Dado** una visita en estado `PENDIENTE`,
- **Cuando** el asesor dueño de la visita (o el Administrador) solicita cancelarla enviando un `motivo` válido,
- **Entonces** la visita cambia su estado a `CANCELADA` (sin borrarse de `Visitas`), se inserta un registro en `HistorialVisitas` con el usuario responsable y motivo, y se crea un aviso en `Avisos`.

### CA-06: Cancelación Automática al Cambiar Estado de Propiedad (`RN-02`, `RN-06`)
- **Dado** una propiedad `DISPONIBLE` que tiene visitas en estado `PENDIENTE`,
- **Cuando** el `ADMINISTRADOR` cambia el estado de la propiedad a `RESERVADA`, `VENDIDA` o `ALQUILADA`,
- **Entonces** todas las visitas pendientes de esa propiedad cambian automáticamente a `CANCELADA`, se registra el evento en `HistorialVisitas` y se notifica en `Avisos`.

### CA-07: Restricción de Permisos para Asesor (`RN-06`, `RN-07`)
- **Dado** un usuario autenticado con rol `ASESOR`,
- **Cuando** intenta crear/editar propiedades, acceder a `/api/auditor` o cancelar la visita de otro asesor,
- **Entonces** el sistema bloquea la operación con código `403 Forbidden` o `400 Bad Request` (`RN-07`).
