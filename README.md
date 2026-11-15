# Sistema de Gestión Inmobiliaria (Spring Boot + Spring Data JPA)

Sistema integral para la gestión de propiedades, clientes, agendamiento inteligente de visitas (60 minutos), reprogramación, cancelación automática en cascada, auditoría de cambios y avisos internos, desarrollado con **Java 21**, **Spring Boot 3.2.5** y **Spring Data JPA / Hibernate** siguiendo la metodología **SDD (Spec-Driven Development)**.

**Autores:** Grecco Cedeño y Britany Tahis Macias

---

## 1. Documentación SDD (`docs/`)

Toda la documentación de ingeniería de software y trazabilidad se encuentra en la carpeta [`docs/`](docs/):

| Documento | Descripción |
| :--- | :--- |
| [`docs/00_REGLAS_PROYECTO.md`](docs/00_REGLAS_PROYECTO.md) | Principios arquitectónicos (las 5 familias de clases), seguridad, transaccionalidad y restricciones del proyecto. |
| [`docs/01_SPEC.md`](docs/01_SPEC.md) | Especificación completa: Historias de Usuario (`HU-01` a `HU-11`), Requerimientos Funcionales (`RF-01` a `RF-13`), Reglas de Negocio (`RN-01` a `RN-10`) y Requerimientos No Funcionales (`RNF-01` a `RNF-03`). |
| [`docs/02_PLAN.md`](docs/02_PLAN.md) | Plan de diseño e implementación por capas, modelo de datos JPA, endpoints REST y estrategia de seguridad. |
| [`docs/03_TASKS.md`](docs/03_TASKS.md) | División detallada del trabajo entre los 2 integrantes del equipo (Grecco Cedeño y Britany Tahis Macias). |
| [`docs/04_VALIDACION.md`](docs/04_VALIDACION.md) | Protocolo de validación, matriz de las **24 pruebas automatizadas (`24/24 PASS`)** y verificación de los 31 puntos de auditoría. |

---

## 2. Arquitectura por Capas (Las 5 Familias de Clases)

El código fuente en `src/main/java/com/inmobiliaria/` implementa estrictamente las **5 familias de clases** del diseño arquitectónico:

1. **Primera Familia — Entities (`com.inmobiliaria.model`)**:
   - `Usuario`, `Propiedad`, `Cliente`, `Visita`, `HistorialVisita`, `Aviso`.
2. **Segunda Familia — Controller (`com.inmobiliaria.controller`)**:
   - `PropiedadesController`, `ClientesController`, `VisitasController`, `AuthController`, `AuditoriaController`, `AvisoController`.
3. **Tercera Familia — Service Interface (`com.inmobiliaria.service`)**:
   - `PropiedadService`, `ClienteService`, `VisitaService`, `AuthService`, `AuditoriaService`, `AvisoService`.
4. **Cuarta Familia — Implementación (`com.inmobiliaria.service.impl`)**:
   - `PropiedadServiceImpl`, `ClienteServiceImpl`, `VisitaServiceImpl` (incluye los 5 métodos privados del diagrama: `validarDisponibilidad`, `calcularHoraFin`, `validarCruceHorario`, `validarAsesor`, `registrarHistorial`), `AuthServiceImpl`, `AuditoriaServiceImpl`, `AvisoServiceImpl`.
5. **Quinta Familia — Repository (`com.inmobiliaria.repository`)**:
   - `PropiedadRepository`, `ClienteRepository`, `UsuarioRepository`, `VisitaRepository`, `HistorialVisitaRepository`, `AvisoRepository`.

---

## 3. Requisitos y Ejecución

### Requisitos Previos
- **Java 21** (JDK 21)
- **Maven 3.9+** (o usar el botón **Run ▶** directamente desde IntelliJ IDEA)

### Iniciar el Servidor
```bash
mvn spring-boot:run
```
También puedes ejecutar la clase `com.inmobiliaria.GestionInmobiliariaApplication` (o `IniciarServidor`) desde IntelliJ IDEA.

Una vez iniciado, abre en el navegador:
- **Aplicación Web**: `http://localhost:3000`

### Credenciales de Prueba (Cifradas con BCrypt)

| Rol | Usuario | Contraseña | Permisos Principales |
| :--- | :---: | :---: | :--- |
| **ADMINISTRADOR** | `admin` | `admin123` | Registrar propiedades (código automático `P-01`, `P-02`...), cambiar estados, gestionar clientes, agendar/reprogramar/cancelar visitas asignando asesor, consultar seguimiento por cliente/propiedad (`HU-10`) e historial de visitas (`HU-09`). |
| **ASESOR** | `carlos` | `carlos123` | Ver propiedades disponibles (`HU-03`), registrar clientes (`HU-04`), agendar (`HU-05`), reprogramar o cancelar sus propias visitas (`HU-06` / `RN-07`), filtrar sus pendientes (`HU-08`) y recibir avisos internos (`HU-11`). |
| **ASESOR** | `laura` | `laura123` | Mismos permisos de asesor sobre su propia agenda (`RN-07`). |

---

## 4. Ejecución de Pruebas Automatizadas (`24/24`)

El proyecto cuenta con una suite de **24 pruebas de integración automatizadas** con JUnit 5 y `MockMvc` (`src/test/java/com/inmobiliaria/GestionInmobiliariaIntegrationTest.java`):

```bash
mvn test
```

---

## 5. Resumen de Endpoints REST

| Método | Ruta | Rol Requerido | Descripción (`HU` / `RF`) |
| :---: | :--- | :---: | :--- |
| `POST` | `/api/auth/login` | Público | Inicia sesión en el servidor (`HttpSession` + cookie `httpOnly`). |
| `POST` | `/api/auth/logout` | Autenticado | Cierra y destruye la sesión activa en el servidor. |
| `GET` | `/api/auth/me` | Autenticado | Devuelve el usuario autenticado en la sesión actual. |
| `GET` | `/api/propiedades` | Autenticado | Lista todas las propiedades o filtra con `?estado=DISPONIBLE` (`HU-03`, `RF-03`). |
| `POST` | `/api/propiedades` | `ADMINISTRADOR` | Registra una propiedad con código automático y `descripcion` (`HU-01`, `RF-01`). |
| `PATCH` | `/api/propiedades/{id}/estado` | `ADMINISTRADOR` | Cambia estado y cancela visitas futuras pendientes si no está disponible (`HU-02`, `HU-07`, `RF-02`, `RF-09`). |
| `GET` | `/api/clientes` | Autenticado | Lista clientes registrados (`RN-09`). |
| `POST` | `/api/clientes` | Autenticado | Registra un cliente (`HU-04`, `RF-04`, `RN-05`). |
| `POST` | `/api/visitas` | Autenticado | Agenda una visita de 60 minutos (`HU-05`, `RF-05`, `RF-06`, `RF-07`). |
| `PATCH` | `/api/visitas/{id}/reprogramar` | Autenticado | Reprograma una visita pendiente exigiendo motivo obligatorio (`HU-06`, `RF-08`, `RN-08`). |
| `PATCH` | `/api/visitas/{id}/cancelar` | Autenticado | Cancela una visita pendiente conservando el registro (`HU-06`, `RF-08`, `RNF-03`). |
| `GET` | `/api/visitas/pendientes` | Autenticado | Filtra visitas pendientes por `?fecha=YYYY-MM-DD` y/o `?asesor=ID` (`HU-08`, `RF-10`). |
| `GET` | `/api/visitas/{id}/historial` | `ADMINISTRADOR` | Consulta el historial de cambios de una visita específica (`HU-09`, `RF-11`). |
| `GET` | `/api/visitas?cliente=ID&propiedad=ID` | `ADMINISTRADOR` | Consulta todas las visitas (incluidas canceladas) por cliente o propiedad (`HU-10`, `RF-12`). |
| `GET` | `/api/visitas/horarios-libres` | Autenticado | Sugiere horarios libres de 60 min dentro del horario de atención (`HU-05`). |
| `GET` | `/api/auditor` | `ADMINISTRADOR` | Consulta el historial global de auditoría (`HU-09`, `RF-11`). |
| `GET` | `/api/avisos` | Autenticado | Lista los avisos internos del asesor o globales para el admin (`HU-11`, `RF-13`, `RN-10`). |
