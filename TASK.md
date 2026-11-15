# Plan de Trabajo, Validación, Pruebas y Cumplimiento (`TASK.md`)

Este documento detalla la **división del trabajo entre 2 integrantes** (Grecco Cedeño y Britany Tahis Macias), el **protocolo de validación y pruebas de QA** y la **matriz de cumplimiento de los 31 puntos de auditoría** sobre la arquitectura **Spring Boot + Spring Data JPA**.

---

## 1. División del Trabajo entre 2 Personas

### 👤 Integrante 1 (Grecco Cedeño): Dominio Propiedades, Clientes, Seguridad y Persistencia JPA
**Responsabilidad principal:** Configuración de Spring Boot y JPA, seguridad con sesiones HTTP y BCrypt (`RNF-01`, `RNF-02`, `RN-09`), módulo de Propiedades (`HU-01`, `HU-02`, `HU-03`, `RN-05`, `RN-06`) y módulo de Clientes (`HU-04`, `RN-05`).

| # | Tarea Asignada | Clases / Archivos Responsables | Estado |
| :---: | :--- | :--- | :---: |
| **1.1** | Configuración de Maven (`pom.xml`), propiedades de conexión JPA (H2 persistente + soporte MySQL) y sembrado con BCrypt. | `pom.xml`, `application.properties`, `DataInitializer.java` | ✅ Completado |
| **1.2** | Primera Familia (`Entities`) y Enumeraciones de Propiedad, Cliente y Usuario (`descripcion` en Propiedad, `WRITE_ONLY` en contraseña). | `Cliente.java`, `Propiedad.java`, `Usuario.java`, `constants/*` | ✅ Completado |
| **1.3** | Quinta Familia (`Repository`) para Propiedad, Cliente y Usuario con Spring Data JPA. | `PropiedadRepository.java`, `ClienteRepository.java`, `UsuarioRepository.java` | ✅ Completado |
| **1.4** | Seguridad real por sesión HTTP (`httpOnly`), interceptor de autenticación (`RN-09`), login/logout con BCrypt y manejo global de errores (`AppException`). | `AuthInterceptor.java`, `WebConfig.java`, `AuthController.java`, `AuthServiceImpl.java`, `GlobalExceptionHandler.java` | ✅ Completado |
| **1.5** | Tercera, Cuarta y Segunda Familia para **Propiedades** (`PropiedadService`, `PropiedadServiceImpl`, `PropiedadesController`) y **Clientes** (`ClienteService`, `ClienteServiceImpl`, `ClientesController`). | `PropiedadService*.java`, `PropiedadesController.java`, `ClienteService*.java`, `ClientesController.java` | ✅ Completado |
| **1.6** | Vista de Login y Panel del Administrador con prevención de XSS (`escapeHtml`). | `index.html`, `login.js`, `admin.html`, `styles.css` | ✅ Completado |

---

### 👤 Integrante 2 (Britany Tahis Macias): Motor de Visitas, Reprogramación, Historial, Seguimiento y Avisos
**Responsabilidad principal:** Módulo central de Visitas (`HU-05` a `HU-11`), agendamiento con validación de asesor (`RN-03`) y cruces de 60 min (`RN-04`), reprogramación (`PATCH /visitas/{id}/reprogramar`), cancelación manual y en cascada solo de visitas futuras (`RN-02`, `RN-08`, `RNF-03`), historial por visita (`RF-11`), seguimiento por cliente/propiedad (`RF-12`) y avisos internos (`RN-10`).

| # | Tarea Asignada | Clases / Archivos Responsables | Estado |
| :---: | :--- | :--- | :---: |
| **2.1** | Primera y Quinta Familia para `Visita`, `HistorialVisita` y `Aviso` (`VisitaRepository`, `HistorialVisitaRepository`, `AvisoRepository`). | `Visita.java`, `HistorialVisita.java`, `Aviso.java`, `*Repository.java` | ✅ Completado |
| **2.2** | Extracción de métodos privados del diagrama de `VisitaServiceImpl`: `validarDisponibilidad`, `calcularHoraFin`, `validarCruceHorario`, `validarAsesor`, `registrarHistorial`. | `VisitaServiceImpl.java` | ✅ Completado |
| **2.3** | Implementación de **Agendar**, **Cancelar** y **Reprogramar** visitas (`reprogramarVisita` excluyendo la propia visita en el cruce y registrando `fechaAnterior` y `fechaNueva` con fecha y hora). | `VisitaService.java`, `VisitaServiceImpl.java`, `VisitasController.java` | ✅ Completado |
| **2.4** | Cancelación automática en cascada **solo de visitas futuras** con el motivo exacto del PDF (`"propiedad reservada"`, `"propiedad vendida"`, `"propiedad alquilada"`). | `VisitaServiceImpl.cancelarVisitasPorPropiedad()` | ✅ Completado |
| **2.5** | Consultas avanzadas: pendientes por fecha/asesor (`RF-10`), historial de una visita (`GET /visitas/{id}/historial` - `RF-11`), seguimiento por cliente o propiedad (`RF-12`), horarios libres (`HU-05`) y avisos (`RF-13`). | `VisitasController.java`, `AuditoriaController.java`, `AvisoController.java` | ✅ Completado |
| **2.6** | Panel del Asesor y lógica interactiva de reprogramación, filtros e historial por visita en frontend. | `asesor.html`, `panel.js` | ✅ Completado |

---

## 2. Matriz de Verificación de los 31 Puntos de Auditoría

| # | Punto Auditado | Estado | Implementación Verificada |
| :---: | :--- | :---: | :--- |
| **1** | Reprogramar visita (`PATCH /visitas/{id}/reprogramar`) | ✅ Cumplido | `VisitasController.reprogramarVisita` y `VisitaServiceImpl.reprogramarVisita` |
| **2** | Historial de una visita (`GET /visitas/{id}/historial`) | ✅ Cumplido | `VisitasController.consultarHistorial` (404 si no existe, 403 si no es Admin) |
| **3** | Visitas por cliente o por propiedad (`GET /visitas?cliente=ID&propiedad=ID`) | ✅ Cumplido | `VisitaServiceImpl.listarPorClienteOPropiedad` (incluye canceladas) |
| **4** | Campo `descripcion` (opcional) en `Propiedad` | ✅ Cumplido | `Propiedad.java`, `admin.html`, `asesor.html`, `panel.js` |
| **5** | Pendientes filtrados por fecha o por asesor (`GET /visitas/pendientes`) | ✅ Cumplido | `VisitaServiceImpl.listarPendientes` (Asesor ignora parámetro ajeno por RN-07) |
| **6** | Fecha y hora completas (`YYYY-MM-DD HH:MM`) en `fechaAnterior` y `fechaNueva` | ✅ Cumplido | `VisitaServiceImpl.formatearFechaHora` y tabla de Auditoría en `admin.html` |
| **7** | Cancelación en cascada solo de visitas futuras con motivos del PDF | ✅ Cumplido | `VisitaServiceImpl.cancelarVisitasPorPropiedad` (`inicioVisita.isAfter(ahora)`) |
| **8** | Sesión en servidor (`httpOnly` cookie), eliminando header `x-usuario-id` | ✅ Cumplido | `AuthInterceptor.java`, `AuthController.java`, `panel.js` |
| **9** | Contraseñas cifradas con BCrypt | ✅ Cumplido | `BCryptPasswordEncoder` en `WebConfig.java` y `DataInitializer.java` |
| **10** | `/api/clientes` protegido con autenticación obligatoria (`RN-09`) | ✅ Cumplido | `AuthInterceptor` protege `/api/clientes` y `/clientes` |
| **11** | Prevención de XSS en el panel web | ✅ Cumplido | Función `escapeHtml()` aplicada en `panel.js` |
| **12** | Validación de quién atiende la visita (`validarAsesor` con rol `ASESOR`) | ✅ Cumplido | `VisitaServiceImpl.validarAsesor` usa `UsuarioRepository` |
| **13** | Validación completa de `Propiedad` (`TipoPropiedad`, `Modalidad`, `EstadoPropiedad`) | ✅ Cumplido | `PropiedadServiceImpl.validarDatos` |
| **14** | Auditoría, avisos y login respetan capas (`Controller -> Service -> Repository`) | ✅ Cumplido | `AuthController/Service`, `AuditoriaController/Service`, `AvisoController/Service` |
| **15** | Uso real de las `Entities` sin exponer contraseñas | ✅ Cumplido | Entidades JPA en `com.inmobiliaria.model` |
| **16** | División de `agendarVisita` en los 5 métodos privados del diagrama | ✅ Cumplido | `VisitaServiceImpl.java` |
| **17** | Códigos HTTP (`400`, `401`, `403`, `404`) y formato `{ error, mensaje }` | ✅ Cumplido | `AppException.java` y `GlobalExceptionHandler.java` |
| **18** | Inyección de dependencias por constructor e interfaces `Service` | ✅ Cumplido | Todas las clases `@RestController` y `@Service` |
| **19-31** | Migración a Spring Boot + JPA, transacciones `@Transactional`, limpieza de repo y horarios libres | ✅ Cumplido | Proyecto Maven Spring Boot completo y limpio |
