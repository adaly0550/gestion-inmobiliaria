# Validación, Matriz de Trazabilidad, Casos de Prueba y Métricas (`04_VALIDACION.md`)

Este documento registra la **validación funcional, de seguridad, arquitectónica y de métricas** del **Sistema de Gestión Inmobiliaria**, cumpliendo los criterios de las láminas 14, 15, 16, 17 y 19 del documento *Proyecto Final - Implementación de Software (IST CENESTUR)*.

---

## 1. Matriz de Trazabilidad Completa (`HU -> RF -> RN -> DER -> Código -> Prueba`)

| HU | RF | RN / RNF | Entidad DER | Clase `Controller` / `ServiceImpl` | Método Principal | Prueba Automatizada (`@Test`) |
| :---: | :---: | :---: | :--- | :--- | :--- | :--- |
| **HU-01** | `RF-01` | `RN-05`, `RN-06` | `Propiedad` | `PropiedadesController`<br>`PropiedadServiceImpl` | `registrarPropiedad()`<br>`validarDatos()` / `validarRol()` | `test04`, `test05`, `test06` |
| **HU-02** | `RF-02` | `RN-02`, `RN-06` | `Propiedad` | `PropiedadesController`<br>`PropiedadServiceImpl` | `cambiarEstado()`<br>`validarReglasEstado()` | `test18` |
| **HU-03** | `RF-03` | `RN-01` | `Propiedad` | `PropiedadesController`<br>`PropiedadServiceImpl` | `listar()` / `listarDisponibles()` | `test07`, `test18` |
| **HU-04** | `RF-04` | `RN-05`, `RN-09` | `Cliente` | `ClientesController`<br>`ClienteServiceImpl` | `registrarCliente()`<br>`validarDatosObligatorios()` | `test03`, `test08`, `test09` |
| **HU-05** | `RF-05`, `RF-06`, `RF-07` | `RN-01`, `RN-03`, `RN-04` | `Visita` | `VisitasController`<br>`VisitaServiceImpl` | `agendarVisita()`<br>`validarDisponibilidad()`<br>`calcularHoraFin()`<br>`validarCruceHorario()`<br>`obtenerHorariosLibres()` | `test10`, `test11`, `test12`, `test13`, `test19`, `test23` |
| **HU-06** | `RF-08` | `RN-07`, `RN-08`, `RNF-03` | `Visita`<br>`HistorialVisita` | `VisitasController`<br>`VisitaServiceImpl` | `reprogramarVisita()`<br>`cancelarVisita()`<br>`registrarHistorial()` | `test14`, `test15`, `test16`, `test17` |
| **HU-07** | `RF-09` | `RN-02`, `RNF-03` | `Visita`<br>`HistorialVisita` | `PropiedadServiceImpl`<br>`VisitaServiceImpl` | `cancelarVisitasPorPropiedad()` | `test18` |
| **HU-08** | `RF-10` | `RN-07` | `Visita` | `VisitasController`<br>`VisitaServiceImpl` | `listarPendientes()` | `test20` |
| **HU-09** | `RF-11` | `RN-06`, `RN-08` | `HistorialVisita` | `VisitasController`<br>`AuditoriaController`<br>`VisitaServiceImpl` | `consultarHistorial()`<br>`listarHistorialGlobal()` | `test21` |
| **HU-10** | `RF-12` | `RN-06`, `RNF-03` | `Visita` | `VisitasController`<br>`VisitaServiceImpl` | `listarPorClienteOPropiedad()` | `test22` |
| **HU-11** | `RF-13` | `RN-10` | `Aviso` | `AvisoController`<br>`VisitaServiceImpl` | `generarAvisoInterno()` | `test10`, `test14`, `test17`, `test18` |
| **Seguridad** | Login / Sesión | `RNF-01`, `RNF-02`, `RN-09` | `Usuario` | `AuthController`<br>`AuthServiceImpl`<br>`AuthInterceptor` | `autenticar()` / `preHandle()` | `test01`, `test02`, `test03`, `test24` |

---

## 2. Evidencia de Pruebas: Casos Positivos y Casos Negativos (`24/24 PASS`)

Comando de ejecución reproducible:
```bash
mvn test
```

| # | Tipo | Caso Probado (`@Test`) | Entrada | Resultado Esperado | Resultado Obtenido | Estado |
| :---: | :---: | :--- | :--- | :--- | :--- | :---: |
| **01** | Positivo | `test01_loginExitosoYCookieHttpOnly` | `POST /api/auth/login` (`admin` / `admin123`) | `200 OK`, crea `HttpSession`, oculta `contrasena` | `200 OK`, sesión activa, sin campo `contrasena` | ✅ Cumple |
| **02** | Negativo | `test02_loginCredencialesInvalidasRetorna401` | `POST /api/auth/login` (`claveErronea`) | `401 Unauthorized` con `{ error, mensaje }` | `401 Unauthorized` con JSON de error | ✅ Cumple |
| **03** | Negativo | `test03_clientesSinSesionRetorna401` | `GET /api/clientes` sin sesión | `401 Unauthorized` (`RN-09`) | `401 Unauthorized` | ✅ Cumple |
| **04** | Positivo | `test04_adminRegistraPropiedadConDescripcionYCodigoAutomatico` | `POST /api/propiedades` (Admin, `VENTA`, con `descripcion`) | `201 Created`, asigna código correlativo `P-11` y estado `DISPONIBLE` | `201 Created`, `codigoInterno = "P-11"` | ✅ Cumple |
| **05** | Negativo | `test05_asesorNoPuedeRegistrarPropiedadRetorna403` | `POST /api/propiedades` con sesión de `carlos` (`ASESOR`) | `403 Forbidden` (`RN-06`) | `403 Forbidden` | ✅ Cumple |
| **06** | Negativo | `test06_validacionTipoYModalidadPropiedadRetorna400` | `POST /api/propiedades` con `tipo = "BodegaInvalida"` | `400 Bad Request` (`RN-05`) | `400 Bad Request` | ✅ Cumple |
| **07** | Positivo | `test07_asesorConsultaPropiedadesDisponibles` | `GET /api/propiedades?estado=DISPONIBLE` | `200 OK`, lista solo inmuebles en estado `DISPONIBLE` (`HU-03`) | `200 OK`, todos con `estado = "DISPONIBLE"` | ✅ Cumple |
| **08** | Positivo | `test08_asesorRegistraClienteConDatosObligatorios` | `POST /api/clientes` (`0912345678`, `Britany Macias`, `0987654321`) | `201 Created` (`HU-04`, `RN-05`) | `201 Created` | ✅ Cumple |
| **09** | Negativo | `test09_clienteSinTelefonoOIdentificacionDuplicadaRetorna400` | `POST /api/clientes` repitiendo cédula `0912345678` | `400 Bad Request` (`RN-05`) | `400 Bad Request` | ✅ Cumple |
| **10** | Positivo / Negativo | `test10_asesorAgendaVisitaCalcula60MinutosYGeneraAviso` | `POST /api/visitas` a las `14:00` en `P-01` + reintento duplicado | `201 Created` (`horaFin = "15:00"`, crea `Aviso`) y `400` al intentar duplicar visita pendiente para la misma propiedad | `201 Created` + `400 Bad Request` en duplicado | ✅ Cumple |
| **11** | Positivo | `test11_adminAgendaVisitaAsignandoAsesorValido` | `POST /api/visitas` por Admin asignando `idAsesor = laura` | `201 Created` tras validar rol `ASESOR` en BD (`RN-03`) | `201 Created` | ✅ Cumple |
| **12** | Negativo | `test12_impedirCruceDeHorarioMismoAsesorRetorna400` | `POST /api/visitas` a las `14:30` cuando asesor tiene cita `14:00-15:00` | `400 Bad Request` por cruce de horario (`RN-04`) | `400 Bad Request` | ✅ Cumple |
| **13** | Negativo | `test13_impedirVisitaEnDomingoOFueraDeHorarioRetorna400` | `POST /api/visitas` en día domingo | `400 Bad Request` (fuera de horario de atención) | `400 Bad Request` | ✅ Cumple |
| **14** | Positivo / Negativo | `test14_asesorReprogramaSuVisitaExcluyendoLaPropiaYGuardaHistorial` | `PATCH /api/visitas/1/reprogramar` a las `15:00` con motivo + reintento al mismo horario | `200 OK` (actualiza `horaFin = "16:00"`, guarda `HistorialVisita` y `Aviso`) y `400` si intenta reprogramar al mismo horario | `200 OK` + `400 Bad Request` al repetir horario | ✅ Cumple |
| **15** | Negativo | `test15_reprogramarSinMotivoRetorna400` | `PATCH /api/visitas/1/reprogramar` con `motivo = "   "` | `400 Bad Request` (`RN-08`) | `400 Bad Request` | ✅ Cumple |
| **16** | Negativo | `test16_asesorNoPuedeReprogramarVisitaAjenaRetorna403` | `PATCH /api/visitas/2/reprogramar` por `carlos` (visita de `laura`) | `403 Forbidden` (`RN-07`) | `403 Forbidden` | ✅ Cumple |
| **17** | Positivo | `test17_asesorCancelaVisitaConservaRegistroYGuardaHistorial` | `PATCH /api/visitas/2/cancelar` con motivo obligatorio | `200 OK`, cambia a `CANCELADA` sin borrar fila (`RNF-03`) | `200 OK`, `estado = "CANCELADA"` | ✅ Cumple |
| **18** | Positivo / Negativo | `test18_cambioEstadoPropiedadCancelaEnCascadaSoloVisitasFuturas` | `PATCH /api/propiedades/3/estado` a `RESERVADA` + pruebas de modalidad incoherente (`VENTA` -> `ALQUILADA`, `ALQUILER` -> `VENDIDA`) | `200 OK` al reservar (cancela visitas futuras pendientes con motivo `"propiedad reservada"`) y `400 Bad Request` en estados incoherentes con la modalidad | `200 OK` + `400 Bad Request` en violaciones de modalidad/estado | ✅ Cumple |
| **19** | Negativo | `test19_noAgendarEnPropiedadNoDisponibleRetorna400` | `POST /api/visitas` y `GET /horarios-libres` sobre propiedad `RESERVADA` | `400 Bad Request` al agendar y `0` horarios disponibles (`RN-01`) | `400 Bad Request` y `[]` | ✅ Cumple |
| **20** | Positivo | `test20_consultarVisitasPendientesPorFechaYPorAsesor` | `GET /api/visitas/pendientes?asesor=idLaura` desde sesión de `carlos` | `200 OK`, devuelve solo visitas de `carlos` (`RN-07`) | `200 OK`, ninguna de `laura` | ✅ Cumple |
| **21** | Positivo / Negativo | `test21_adminConsultaHistorialDeUnaVisitaEspecifica` | `GET /api/visitas/1/historial` (Admin vs Asesor vs ID inexistente) | `200 OK` para Admin (`fechaAnterior`, `fechaNueva`), `403` para Asesor, `404` si no existe | `200 OK`, `403` y `404` | ✅ Cumple |
| **22** | Positivo | `test22_adminConsultaVisitasPorClienteYPorPropiedadIncluyendoCanceladas` | `GET /api/visitas?cliente=1` y `?propiedad=2` | `200 OK`, incluye visitas `PENDIENTE` y `CANCELADA` (`HU-10`) | `200 OK` | ✅ Cumple |
| **23** | Positivo | `test23_consultarHorariosLibresSugeridos` | `GET /api/visitas/horarios-libres?fecha=...` | `200 OK`, lista bloques de 60 minutos disponibles | `200 OK`, incluye `"09:00"` | ✅ Cumple |
| **24** | Positivo | `test24_logoutDestruyeSesionEnServidor` | `POST /api/auth/logout` seguido de `GET /api/auth/me` | `200 OK` al salir y `401 Unauthorized` después | `200 OK` y `401 Unauthorized` | ✅ Cumple |

---

## 3. Métricas del Proyecto e Interpretación de Calidad

De acuerdo con la lámina 15 del documento de consigna (*Métricas: No basta con decir que el software quedó bien*), se midieron e interpretaron las siguientes 5 métricas objetivas:

| # | Métrica Seleccionada | Dato Medido | Interpretación del Resultado | Decisión / Mejora Aplicada |
| :---: | :--- | :---: | :--- | :--- |
| **1** | **Porcentaje de Casos de Prueba Automatizados Exitosos** | **24 / 24 (`100 %` de éxito)**<br>*(En la primera corrida se detectó 1 fallo en `test20` -> `23/24 = 95,8 %`; se corrigió y alcanzó `24/24 = 100 %`)* | Confirma que tanto los flujos principales (casos positivos) como los bloqueos por reglas de negocio y permisos (casos negativos) responden exactamente según `01_SPEC.md`. | Se ampliaron las pruebas `test10`, `test14`, `test18` y `test19` para validar también la coherencia `Modalidad vs Estado (RESERVADA)` y el bloqueo de duplicados. |
| **2** | **Cumplimiento de Hitos y Puntos de Auditoría** | **4 / 4 Hitos (`100 %`)**<br>**31 / 31 Puntos de Auditoría (`100 %`)** | Todos los requisitos (`HU-01` a `HU-11`, `RF-01` a `RF-13`, `RN-01` a `RN-10`, `RNF-01` a `RNF-03`) están trazados desde el modelo hasta la base de datos. | Se completaron los 5 artefactos SDD en `docs/`, el script SQL en `sql/bd_gestion_inmobiliaria.sql` y el catálogo inicial de 10 propiedades (`P-01` a `P-10`). |
| **3** | **Defectos Encontrados en Auditoría y Corregidos** | **8 defectos detectados y 8 corregidos (`100 %` resueltos)** | Durante la revisión de código generado con IA se detectaron: ausencia de validación `Modalidad vs Estado`, formulario de reprogramación que permanecía abierto generando envíos repetidos, filtro `?estado=RESERVADA` no aplicado y horarios libres sugeridos en propiedades reservadas. | Se implementó `validarReglasEstado()`, `validarSinDuplicadoClientePropiedad()`, bloqueo de reprogramación al mismo horario y cierre automático del formulario tras guardar. |
| **4** | **Acoplamiento y Separación por Capas (Mantenibilidad)** | **44 clases Java organizadas en 5 familias (`0` accesos directos de `Controller` a `Repository`)** | El 100 % de los controladores dependen únicamente de interfaces `Service`, y el 100 % de las reglas de negocio residen en `ServiceImpl` bajo transacciones `@Transactional`. | Se extrajeron en `VisitaServiceImpl` los 5 métodos privados del diagrama de clases (`validarDisponibilidad`, `calcularHoraFin`, `validarCruceHorario`, `validarAsesor`, `registrarHistorial`). |
| **5** | **Tiempo de Ejecución de Pruebas y Respuesta de API** | **Suite completa (24 tests): `~19,5 s`**<br>**Tiempo medio por petición REST local: `< 25 ms`** | Las consultas parametrizadas de Spring Data JPA y el índice de claves únicas permiten validar cruces de horario en tiempo real sin degradar la experiencia de usuario. | Se agregaron botones de selección rápida de horarios libres y deshabilitación del botón de envío durante la petición HTTP para evitar dobles clics. |
