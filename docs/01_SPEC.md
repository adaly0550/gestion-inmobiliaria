# Especificación del Sistema (`01_SPEC.md`)

Este documento detalla la especificación técnica y funcional del **Sistema de Gestión Inmobiliaria**, alineando exactamente los identificadores de **Historias de Usuario (`HU-01` a `HU-11`)**, **Requerimientos Funcionales (`RF-01` a `RF-13`)**, **Reglas de Negocio (`RN-01` a `RN-10`)** y **Requerimientos No Funcionales (`RNF-01` a `RNF-03`)** con el documento oficial de análisis y diseño (*Gestión Inmobiliaria - Grecco Cedeño y Britany Tahis Macias*).

---

## 1. Roles del Sistema

| Rol | Descripción y Alcance | Credenciales Semilla (BCrypt) |
| :--- | :--- | :--- |
| **ADMINISTRADOR** | Registra propiedades, cambia su estado, gestiona clientes, agenda/cancela/reprograma visitas asignando un asesor, filtra visitas pendientes por asesor/fecha, consulta el seguimiento por cliente o propiedad y revisa el historial de cambios de cada visita. | `admin` / `admin123` |
| **ASESOR** | Consulta propiedades disponibles, registra clientes, agenda/cancela/reprograma exclusivamente sus propias visitas, consulta sus visitas pendientes y recibe avisos internos. | `carlos` / `carlos123`<br>`laura` / `laura123` |

---

## 2. Historias de Usuario (`HU-01` a `HU-11`)

| ID | Título | Historia de Usuario | Epic | Prioridad |
| :--- | :--- | :--- | :--- | :---: |
| **HU-01** | Registrar propiedad | Como administrador quiero registrar una propiedad con su código, dirección, tipo, modalidad, descripción y estado para tenerla en el sistema. | Propiedades | Alta |
| **HU-02** | Cambiar estado de propiedad | Como administrador quiero cambiar el estado de una propiedad a disponible, reservada, vendida o alquilada para saber si todavía puede recibir visitas. | Propiedades | Alta |
| **HU-03** | Consultar propiedades disponibles | Como asesor quiero ver qué propiedades están disponibles para ofrecérselas a los clientes. | Propiedades | Alta |
| **HU-04** | Registrar cliente | Como asesor quiero registrar un cliente con su identificación, nombres, teléfono y, si lo tiene, su correo para poder asociarlo a una visita. | Clientes | Alta |
| **HU-05** | Agendar visita | Como asesor quiero agendar una visita con cliente, propiedad, fecha y hora, y que el sistema solo me deje elegir propiedades disponibles y horarios libres de mi agenda. | Visitas | Alta |
| **HU-06** | Cancelar o reprogramar visita | Como asesor quiero cancelar o reprogramar una de mis visitas indicando el motivo para que el cambio quede guardado en el historial. | Visitas | Alta |
| **HU-07** | Cancelar visitas por cambio de estado | Como administrador quiero que, al marcar una propiedad como reservada, vendida o alquilada, sus visitas pendientes futuras se cancelen solas. | Visitas | Alta |
| **HU-08** | Consultar visitas pendientes | Como asesor o administrador quiero ver las visitas pendientes por fecha o por asesor (el asesor solo ve las suyas) para organizar la agenda. | Visitas | Alta |
| **HU-09** | Ver historial de una visita | Como administrador quiero revisar los cambios de una visita para saber quién la canceló o reprogramó, cuándo y por qué. | Visitas | Media |
| **HU-10** | Consultar visitas por cliente o propiedad | Como administrador quiero ver todas las visitas de un cliente o de una propiedad para conocer su seguimiento. | Visitas | Media |
| **HU-11** | Recibir aviso interno | Como asesor quiero recibir un aviso dentro del sistema cuando se agenda o cambia una de mis visitas para enterarme sin revisar toda la agenda. | Visitas | Media |

---

## 3. Requerimientos Funcionales (`RF-01` a `RF-13`)

| ID | Requerimiento Funcional | Historia Origen | Ruta HTTP / Método Service |
| :--- | :--- | :--- | :--- |
| **RF-01** | Permitir al administrador registrar propiedades con código interno (automático o manual), dirección, tipo (`Casa`/`Departamento`), modalidad (`VENTA`/`ALQUILER`), descripción y estado. | HU-01 | `POST /propiedades` (`/api/propiedades`)<br>`PropiedadService.registrarPropiedad()` |
| **RF-02** | Permitir solo al administrador cambiar el estado de una propiedad a `DISPONIBLE`, `RESERVADA`, `VENDIDA` o `ALQUILADA`. | HU-02 | `PATCH /propiedades/{id}/estado`<br>`PropiedadService.cambiarEstado()` |
| **RF-03** | Mostrar la lista de propiedades disponibles. | HU-03 | `GET /propiedades?estado=DISPONIBLE`<br>`PropiedadService.listarDisponibles()` |
| **RF-04** | Permitir registrar clientes con identificación, nombres y teléfono obligatorios, y correo opcional. | HU-04 | `POST /clientes` (`/api/clientes`)<br>`ClienteService.registrarCliente()` |
| **RF-05** | Permitir agendar una visita con cliente, propiedad, asesor, fecha y hora. | HU-05 | `POST /visitas` (`/api/visitas`)<br>`VisitaService.agendarVisita()` |
| **RF-06** | Permitir agendar visitas solo en propiedades con estado `DISPONIBLE`. | HU-05 | `VisitaServiceImpl.validarDisponibilidad()` |
| **RF-07** | Impedir que un asesor tenga dos visitas que se crucen, tomando 60 minutos como duración de cada visita. | HU-05 | `VisitaServiceImpl.calcularHoraFin()`<br>`VisitaServiceImpl.validarCruceHorario()` |
| **RF-08** | Permitir cancelar o reprogramar una visita, pidiendo siempre un motivo obligatorio. | HU-06 | `PATCH /visitas/{id}/cancelar`<br>`PATCH /visitas/{id}/reprogramar`<br>`cancelarVisita()` / `reprogramarVisita()` |
| **RF-09** | Cancelar las visitas futuras pendientes de una propiedad cuando su estado cambie a `RESERVADA`, `VENDIDA` o `ALQUILADA`. | HU-07 | `VisitaService.cancelarVisitasPorPropiedad()` |
| **RF-10** | Mostrar las visitas pendientes por fecha o por asesor. Cada asesor solo ve las suyas. | HU-08 | `GET /visitas/pendientes?fecha=...&asesor=...`<br>`VisitaService.listarPendientes()` |
| **RF-11** | Guardar y consultar el historial de cada visita con motivo, fecha/hora del cambio, usuario, `fechaAnterior` y `fechaNueva` (`YYYY-MM-DD HH:MM`). | HU-06, HU-09 | `GET /visitas/{id}/historial` y `GET /api/auditor`<br>`VisitaService.consultarHistorial()` |
| **RF-12** | Mostrar todas las visitas (incluidas canceladas) de un cliente o de una propiedad. | HU-10 | `GET /visitas?cliente=...` o `?propiedad=...`<br>`VisitaService.listarPorClienteOPropiedad()` |
| **RF-13** | Enviar y almacenar un aviso interno al asesor cuando se agenda, cancela o reprograma una de sus visitas. | HU-11 | `GET /api/avisos`<br>`VisitaService.generarAvisoInterno()` |

---

## 4. Reglas de Negocio (`RN-01` a `RN-10`)

| ID | Regla de Negocio | Requerimientos Asociados | Implementación en Código |
| :--- | :--- | :--- | :--- |
| **RN-01** | Solo una propiedad en estado `DISPONIBLE` puede recibir visitas (ni para agendar ni para reprogramar). | RF-06 | `VisitaServiceImpl.validarDisponibilidad()` |
| **RN-02** | Si una propiedad pasa a `RESERVADA`, `VENDIDA` o `ALQUILADA`, sus visitas futuras pendientes se cancelan automáticamente con el motivo `"propiedad reservada"`, `"propiedad vendida"` o `"propiedad alquilada"`. | RF-09 | `PropiedadServiceImpl.cambiarEstado()`<br>`VisitaServiceImpl.cancelarVisitasPorPropiedad()` |
| **RN-03** | Cada visita debe tener cliente, propiedad, asesor (con rol `ASESOR`), fecha y hora de inicio. | RF-05 | `VisitaServiceImpl.agendarVisita()`<br>`VisitaServiceImpl.validarAsesor()` |
| **RN-04** | Cada visita dura exactamente **60 minutos** y no puede existir cruce de horario para el asesor, el cliente ni la propiedad (excluyendo la propia visita al reprogramar). | RF-07 | `VisitaServiceImpl.calcularHoraFin()`<br>`VisitaServiceImpl.validarCruceHorario()` |
| **RN-05** | Un cliente no se registra sin identificación (única), nombres y teléfono (correo opcional). Una propiedad no se registra sin código interno (único), dirección, tipo (`Casa`/`Departamento`), modalidad (`VENTA`/`ALQUILER`) y estado (descripción opcional). | RF-01, RF-04 | `ClienteServiceImpl.validarDatosObligatorios()`<br>`PropiedadServiceImpl.validarDatos()` |
| **RN-06** | Solo el `ADMINISTRADOR` registra propiedades, cambia el estado de una propiedad, registra asesores y consulta el historial/seguimiento global. | RF-01, RF-02, RNF-02 | `PropiedadServiceImpl.validarRol()`<br>`VisitaServiceImpl.consultarHistorial()` |
| **RN-07** | El `ASESOR` solo maneja (agenda, cancela, reprograma) y visualiza sus propias visitas. Solo usuarios con rol `ASESOR` atienden visitas. | RF-08, RF-10, RNF-02 | `VisitaServiceImpl.cancelarVisita()`<br>`reprogramarVisita()` / `listarPendientes()` |
| **RN-08** | Toda cancelación o reprogramación exige un motivo obligatorio y queda registrada en `HistorialVisita`. Ninguna visita se borra. | RF-08, RF-11, RNF-03 | `VisitaServiceImpl.registrarHistorial()` |
| **RN-09** | Solo asesores y administradores autenticados usan el sistema y registran o consultan datos. El cliente no tiene usuario en el sistema. | RF-05, RNF-01 | `AuthInterceptor` (`WebConfig`) |
| **RN-10** | Cuando se agenda, cancela o reprograma una visita, el asesor dueño de la visita recibe un aviso interno persistido en la entidad `Aviso`. | RF-13 | `VisitaServiceImpl.generarAvisoInterno()` |

---

## 5. Requerimientos No Funcionales (`RNF-01` a `RNF-03`)

| ID | Tipo | Requerimiento | Implementación |
| :--- | :--- | :--- | :--- |
| **RNF-01** | Seguridad | El sistema debe pedir usuario y contraseña para entrar y mantener sesión en el servidor (`httpOnly`) con contraseñas cifradas con BCrypt. | `AuthController`, `AuthServiceImpl`, `AuthInterceptor` |
| **RNF-02** | Seguridad | El sistema debe validar el rol del usuario en el servidor antes de permitir cada acción. | `PropiedadServiceImpl.validarRol()`, `VisitaServiceImpl` |
| **RNF-03** | Integridad de datos | El sistema no debe borrar ninguna visita, solo cambiar su estado a `CANCELADA` y conservar su trazabilidad en `HistorialVisita`. | `VisitaRepository`, `HistorialVisitaRepository` |
