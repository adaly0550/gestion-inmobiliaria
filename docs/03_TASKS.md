# División de Tareas del Proyecto (`03_TASKS.md`)

Este documento detalla la **división equitativa del trabajo entre los 2 integrantes** del equipo (**Grecco Cedeño** y **Britany Tahis Macias**), organizada por fases y módulos sobre la arquitectura **Spring Boot + Spring Data JPA**.

---

## 1. Asignación por Integrante

### 👤 Integrante 1: Grecco Cedeño
**Rol en el equipo:** Dominio de Propiedades, Clientes, Seguridad, Persistencia JPA y Panel de Administración.  
**Historias y Reglas cubiertas:** `HU-01`, `HU-02`, `HU-03`, `HU-04`, `RF-01`, `RF-02`, `RF-03`, `RF-04`, `RN-01`, `RN-05`, `RN-06`, `RN-09`, `RNF-01`, `RNF-02`.

| Código | Tarea | Archivos / Clases Entregables | Estado |
| :---: | :--- | :--- | :---: |
| **T-01** | Configuración del proyecto Maven (`pom.xml`), conexión JPA (`application.properties`) y datos semilla con BCrypt (`DataInitializer`). | `pom.xml`, `application.properties`, `DataInitializer.java` | ✅ Completado |
| **T-02** | Creación de Enumeraciones (`constants/`) y Entidades JPA para `Usuario`, `Propiedad` (con `descripcion` y código automático) y `Cliente`. | `Usuario.java`, `Propiedad.java`, `Cliente.java`, `constants/*` | ✅ Completado |
| **T-03** | Creación de Repositorios JPA (`PropiedadRepository`, `ClienteRepository`, `UsuarioRepository`). | `PropiedadRepository.java`, `ClienteRepository.java`, `UsuarioRepository.java` | ✅ Completado |
| **T-04** | Implementación de Seguridad: sesión HTTP (`httpOnly`), `AuthInterceptor`, `AuthService`/`AuthServiceImpl`, `AuthController` y `GlobalExceptionHandler` (`AppException`). | `AuthInterceptor.java`, `WebConfig.java`, `AuthController.java`, `AuthServiceImpl.java`, `GlobalExceptionHandler.java` | ✅ Completado |
| **T-05** | Implementación de `PropiedadService` / `PropiedadServiceImpl` (`validarDatos`, `validarRol`, código interno automático) y `PropiedadesController`. | `PropiedadService.java`, `PropiedadServiceImpl.java`, `PropiedadesController.java` | ✅ Completado |
| **T-06** | Implementación de `ClienteService` / `ClienteServiceImpl` (`validarDatosObligatorios`) y `ClientesController`. | `ClienteService.java`, `ClienteServiceImpl.java`, `ClientesController.java` | ✅ Completado |
| **T-07** | Desarrollo de la interfaz de Login (`index.html`, `login.js`) y Panel de Administrador (`admin.html`, `styles.css`). | `index.html`, `login.js`, `admin.html`, `styles.css` | ✅ Completado |

---

### 👤 Integrante 2: Britany Tahis Macias
**Rol en el equipo:** Motor de Visitas, Reprogramación, Cancelación en Cascada, Historial, Seguimiento, Avisos y Pruebas Automatizadas.  
**Historias y Reglas cubiertas:** `HU-05`, `HU-06`, `HU-07`, `HU-08`, `HU-09`, `HU-10`, `HU-11`, `RF-05` a `RF-13`, `RN-02`, `RN-03`, `RN-04`, `RN-07`, `RN-08`, `RN-10`, `RNF-03`.

| Código | Tarea | Archivos / Clases Entregables | Estado |
| :---: | :--- | :--- | :---: |
| **T-08** | Creación de Entidades JPA y Repositorios para `Visita`, `HistorialVisita` y `Aviso`. | `Visita.java`, `HistorialVisita.java`, `Aviso.java`, `VisitaRepository.java`, `HistorialVisitaRepository.java`, `AvisoRepository.java` | ✅ Completado |
| **T-09** | Implementación de los 5 métodos privados del diagrama de `VisitaServiceImpl`: `validarDisponibilidad`, `calcularHoraFin`, `validarCruceHorario`, `validarAsesor`, `registrarHistorial`. | `VisitaServiceImpl.java` | ✅ Completado |
| **T-10** | Implementación de `agendarVisita`, `cancelarVisita` y `reprogramarVisita` (`PATCH /visitas/{id}/reprogramar`) con exclusión de la propia visita y registro de `fechaAnterior` y `fechaNueva` (`YYYY-MM-DD HH:MM`). | `VisitaService.java`, `VisitaServiceImpl.java`, `VisitasController.java` | ✅ Completado |
| **T-11** | Implementación de `cancelarVisitasPorPropiedad` cancelando únicamente visitas futuras pendientes con los motivos oficiales del PDF (`"propiedad reservada"`, `"propiedad vendida"`, `"propiedad alquilada"`). | `VisitaServiceImpl.java` | ✅ Completado |
| **T-12** | Implementación de consultas: `listarPendientes` (`GET /visitas/pendientes`), `consultarHistorial` (`GET /visitas/{id}/historial`), `listarPorClienteOPropiedad` (`GET /visitas?cliente=...&propiedad=...`) y `obtenerHorariosLibres`. | `VisitaServiceImpl.java`, `VisitasController.java` | ✅ Completado |
| **T-13** | Implementación de `AuditoriaService`/`AuditoriaController` y `AvisoService`/`AvisoController` respetando las 5 familias de capas. | `AuditoriaService*.java`, `AuditoriaController.java`, `AvisoService*.java`, `AvisoController.java` | ✅ Completado |
| **T-14** | Desarrollo del Panel de Asesor (`asesor.html`), tarjeta visual de reprogramación y lógica de frontend (`panel.js` con `escapeHtml`), más suite de 24 pruebas automatizadas (`GestionInmobiliariaIntegrationTest.java`). | `asesor.html`, `panel.js`, `GestionInmobiliariaIntegrationTest.java` | ✅ Completado |
