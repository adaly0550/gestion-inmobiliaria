# Validación, Pruebas Automatizadas y Cumplimiento (`04_VALIDACION.md`)

Este documento registra la **validación funcional, de seguridad y arquitectónica** del **Sistema de Gestión Inmobiliaria**, respaldada por **24 pruebas de integración automatizadas** en JUnit 5 y Spring Boot `MockMvc` ([GestionInmobiliariaIntegrationTest.java](../src/test/java/com/inmobiliaria/GestionInmobiliariaIntegrationTest.java)) que se ejecutan con el comando `mvn test`.

---

## 1. Ejecución de la Suite Automatizada (`24/24 PASS`)

Para ejecutar todas las pruebas automatizadas del proyecto:

```bash
mvn test
```

### Matriz de las 24 Pruebas Automatizadas (`GestionInmobiliariaIntegrationTest`)

| # | Método de Prueba (`@Test`) | HU / RF / RN / RNF Verificado | Resultado Esperado | Estado |
| :---: | :--- | :--- | :--- | :---: |
| **01** | `test01_loginExitosoYCookieHttpOnly` | `RNF-01`, Punto 8, Punto 9 | Login con BCrypt crea sesión en servidor y no expone `contrasena` en JSON. | ✅ PASS |
| **02** | `test02_loginCredencialesInvalidasRetorna401` | `RNF-01`, Punto 17 | Credenciales incorrectas devuelven `401` con formato `{ error, mensaje }`. | ✅ PASS |
| **03** | `test03_clientesSinSesionRetorna401` | `RN-09`, Punto 10 | `GET /api/clientes` sin sesión activa devuelve `401 Unauthorized`. | ✅ PASS |
| **04** | `test04_adminRegistraPropiedadConDescripcionYCodigoAutomatico` | `HU-01`, `RF-01`, `RN-05`, Punto 4 | Admin registra propiedad con `descripcion` y se genera `codigoInterno` (`P-03`). | ✅ PASS |
| **05** | `test05_asesorNoPuedeRegistrarPropiedadRetorna403` | `RN-06`, `RNF-02` | Asesor intenta `POST /api/propiedades` y recibe `403 Forbidden`. | ✅ PASS |
| **06** | `test06_validacionTipoYModalidadPropiedadRetorna400` | `RN-05`, Punto 13 | Tipo o modalidad fuera de los enums permitidos devuelve `400 Bad Request`. | ✅ PASS |
| **07** | `test07_asesorConsultaPropiedadesDisponibles` | `HU-03`, `RF-03`, `RN-01` | `GET /api/propiedades?estado=DISPONIBLE` devuelve solo inmuebles disponibles. | ✅ PASS |
| **08** | `test08_asesorRegistraClienteConDatosObligatorios` | `HU-04`, `RF-04`, `RN-05` | Asesor registra cliente con identificación, nombre y teléfono obligatorios. | ✅ PASS |
| **09** | `test09_clienteSinTelefonoOIdentificacionDuplicadaRetorna400` | `RN-05` | Cliente sin datos obligatorios o con identificación repetida devuelve `400`. | ✅ PASS |
| **10** | `test10_asesorAgendaVisitaCalcula60MinutosYGeneraAviso` | `HU-05`, `HU-11`, `RF-05`, `RF-07`, `RF-13`, `RN-04`, `RN-10` | Agenda visita en propiedad disponible, calcula `horaFin = +60 min` y crea aviso interno. | ✅ PASS |
| **11** | `test11_adminAgendaVisitaAsignandoAsesorValido` | `RN-03`, `RN-07`, Punto 12 | Admin agenda visita asignando un usuario con rol `ASESOR` validado en BD. | ✅ PASS |
| **12** | `test12_impedirCruceDeHorarioMismoAsesorRetorna400` | `RF-07`, `RN-04` | Intentar agendar otra visita que se solape en los 60 min del asesor devuelve `400`. | ✅ PASS |
| **13** | `test13_impedirVisitaEnDomingoOFueraDeHorarioRetorna400` | `RN-04`, Horario Atención | Agendar en domingo o fuera de horario de atención devuelve `400`. | ✅ PASS |
| **14** | `test14_asesorReprogramaSuVisitaExcluyendoLaPropiaYGuardaHistorial` | `HU-06`, `RF-08`, `RF-11`, `RN-08`, Puntos 1 y 6 | Reprograma visita a hora contigua sin chocar consigo misma y guarda `fechaAnterior` y `fechaNueva` (`YYYY-MM-DD HH:MM`). | ✅ PASS |
| **15** | `test15_reprogramarSinMotivoRetorna400` | `RF-08`, `RN-08` | `PATCH /api/visitas/{id}/reprogramar` sin motivo devuelve `400`. | ✅ PASS |
| **16** | `test16_asesorNoPuedeReprogramarVisitaAjenaRetorna403` | `RN-07`, `RNF-02` | Un asesor intenta reprogramar la visita de otro asesor y recibe `403`. | ✅ PASS |
| **17** | `test17_asesorCancelaVisitaConservaRegistroYGuardaHistorial` | `HU-06`, `RF-08`, `RN-08`, `RNF-03` | Cancela visita con motivo; estado pasa a `CANCELADA` sin borrarse de la BD. | ✅ PASS |
| **18** | `test18_cambioEstadoPropiedadCancelaEnCascadaSoloVisitasFuturas` | `HU-02`, `HU-07`, `RF-02`, `RF-09`, `RN-02`, Punto 7 | Cambiar propiedad a `RESERVADA`/`VENDIDA` cancela sus visitas futuras pendientes con el motivo exacto del PDF. | ✅ PASS |
| **19** | `test19_noAgendarEnPropiedadNoDisponibleRetorna400` | `RF-06`, `RN-01` | Intentar agendar en propiedad `RESERVADA` o `VENDIDA` devuelve `400`. | ✅ PASS |
| **20** | `test20_consultarVisitasPendientesPorFechaYPorAsesor` | `HU-08`, `RF-10`, `RN-07`, Punto 5 | Admin filtra por `?asesor=ID`; Asesor ignora parámetro ajeno y solo ve las suyas. | ✅ PASS |
| **21** | `test21_adminConsultaHistorialDeUnaVisitaEspecifica` | `HU-09`, `RF-11`, Punto 2 | `GET /api/visitas/{id}/historial` devuelve historial ordenado; `404` si no existe y `403` si es asesor. | ✅ PASS |
| **22** | `test22_adminConsultaVisitasPorClienteYPorPropiedadIncluyendoCanceladas` | `HU-10`, `RF-12`, `RNF-03`, Punto 3 | `GET /api/visitas?cliente=ID` y `?propiedad=ID` devuelven todas las visitas incluidas las `CANCELADA`. | ✅ PASS |
| **23** | `test23_consultarHorariosLibresSugeridos` | `HU-05`, Punto 31 | `GET /api/visitas/horarios-libres` devuelve los bloques de 60 min disponibles. | ✅ PASS |
| **24** | `test24_logoutDestruyeSesionEnServidor` | `RNF-01`, Punto 8 | `POST /api/auth/logout` invalida la sesión y peticiones posteriores devuelven `401`. | ✅ PASS |

---

## 2. Resumen de Cumplimiento de los 31 Puntos de Auditoría

| Categoría | Puntos | Estado |
| :--- | :---: | :---: |
| **A. Funciones del PDF (Puntos 1 al 7)** | Reprogramar visita (`PATCH /visitas/{id}/reprogramar`), historial por visita (`GET /visitas/{id}/historial`), seguimiento por cliente/propiedad (`GET /visitas?cliente=...&propiedad=...`), campo `descripcion`, filtro pendientes por asesor (`GET /visitas/pendientes?asesor=...`), fecha y hora completas en historial (`YYYY-MM-DD HH:MM`), cancelación en cascada solo de visitas futuras. | ✅ **7/7 Completos** |
| **B. Seguridad (Puntos 8 al 11)** | Sesión en servidor (`HttpSession` + cookie `httpOnly`, sin confiar en header `x-usuario-id`), contraseñas con BCrypt, `/api/clientes` protegido con login (`RN-09`), prevención de XSS con `escapeHtml()`. | ✅ **4/4 Completos** |
| **C. Reglas de Negocio y Arquitectura (Puntos 12 al 24)** | `validarAsesor()` con `UsuarioRepository`, validación completa de `Propiedad`, 5 familias de capas respetadas en los 6 módulos, uso real de `Entities` sin exponer `contrasena`, extracción de los 5 métodos privados en `VisitaServiceImpl`, códigos HTTP exactos (`400`, `401`, `403`, `404`) con `{ error, mensaje }`, inyección por constructor e interfaces `Service`. | ✅ **13/13 Completos** |
| **D. Base de Datos, Repo y Documentación SDD (Puntos 25 al 31)** | Transacciones `@Transactional`, llaves foráneas JPA, limpieza de `.gitignore`, carpeta `docs/` completa (`00_REGLAS_PROYECTO.md` a `04_VALIDACION.md`), `README.md` y sugerencia de horarios libres (`HU-05`). | ✅ **7/7 Completos** |
