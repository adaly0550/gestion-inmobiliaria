# Plan de Arquitectura, Modelos y Diseño (`02_PLAN.md`)

Este documento describe el **Plan de Diseño e Implementación** del **Sistema de Gestión Inmobiliaria**, estructurado bajo la metodología **SDD (Spec-Driven Development)** e integrando el **DFD**, el **DER**, la **Arquitectura por Capas** y el **Diagrama de Clases (5 Familias)** exigidos en la consigna del Proyecto Final del IST CENESTUR.

---

## 1. Conexión entre Modelos: DFD, DER y Clases

Siguiendo la regla de correlación del proyecto:
- **Los procesos del DFD** determinan las operaciones expuestas en los `Controller` y `Service`.
- **Las entidades del DER** se mapean 1 a 1 en la primera familia (`Entities` JPA en `com.inmobiliaria.model`).
- **Las Historias de Usuario (`HU`) y Requisitos Funcionales (`RF`)** definen los contratos en `Service`, y **las Reglas de Negocio (`RN-01` a `RN-10`)** se implementan exclusivamente en `ServiceImpl`.

### 1.1. Diagrama de Flujo de Datos (DFD - Nivel 1)

```mermaid
flowchart LR
    ADMIN["Actor: ADMINISTRADOR"]
    ASESOR["Actor: ASESOR"]

    P1(("1.0 Autenticación\ny Sesión\n(RNF-01, RNF-02)"))
    P2(("2.0 Gestión de\nPropiedades y Reservación\n(HU-01 a HU-03, RN-01, RN-02)"))
    P3(("3.0 Gestión de\nClientes\n(HU-04, RN-05)"))
    P4(("4.0 Agendamiento,\nReprogramación y Cancelación\n(HU-05 a HU-08, RN-03 a RN-08)"))
    P5(("5.0 Auditoría y\nAvisos Internos\n(HU-09 a HU-11, RN-10)"))

    DB_U[("Tabla: usuarios")]
    DB_P[("Tabla: propiedades")]
    DB_C[("Tabla: clientes")]
    DB_V[("Tabla: visitas")]
    DB_H[("Tabla: historial_visitas")]
    DB_A[("Tabla: avisos")]

    ADMIN -->|Credenciales| P1
    ASESOR -->|Credenciales| P1
    P1 <-->|Verifica BCrypt y Rol| DB_U

    ADMIN -->|Registra / Cambia estado a RESERVADA, VENDIDA, ALQUILADA| P2
    ASESOR -->|Consulta DISPONIBLES / RESERVADAS| P2
    P2 <-->|Persiste inmueble y estado| DB_P
    P2 -->|RN-02: Cancela visitas futuras en cascada| P4

    ADMIN -->|Registra / Consulta| P3
    ASESOR -->|Registra / Consulta| P3
    P3 <-->|Valida cédula única| DB_C

    ADMIN -->|Agenda asignando asesor / Filtra / Cancela / Reprograma| P4
    ASESOR -->|Agenda / Cancela / Reprograma sus propias visitas| P4
    P4 <-->|Valida RN-01, 60 min y cruces RN-04| DB_V
    P4 -->|RN-08: Registra motivo y fechas| DB_H
    P4 -->|RN-10: Notifica al asesor| DB_A

    ADMIN -->|Consulta historial y seguimiento HU-09 / HU-10| P5
    ASESOR -->|Consulta sus avisos HU-11| P5
    P5 <--> DB_H
    P5 <--> DB_A
```

---

### 1.2. Diagrama Entidad-Relación (DER)

```mermaid
erDiagram
    USUARIOS ||--o{ VISITAS : "atiende (id_usuario)"
    USUARIOS ||--o{ HISTORIAL_VISITAS : "registra cambio (id_usuario_responsable)"
    USUARIOS ||--o{ AVISOS : "recibe (id_usuario)"
    CLIENTES ||--o{ VISITAS : "solicita (id_cliente)"
    PROPIEDADES ||--o{ VISITAS : "recibe (id_propiedad)"
    VISITAS ||--o{ HISTORIAL_VISITAS : "genera trazabilidad (id_visita)"

    USUARIOS {
        BIGINT id PK
        VARCHAR nombres
        VARCHAR usuario UK
        VARCHAR contrasena "BCrypt"
        VARCHAR rol "ADMINISTRADOR | ASESOR"
    }
    PROPIEDADES {
        BIGINT id PK
        VARCHAR codigo_interno UK "P-01..P-10+"
        VARCHAR direccion
        VARCHAR tipo "Casa | Departamento"
        VARCHAR modalidad "VENTA | ALQUILER"
        VARCHAR descripcion
        VARCHAR estado "DISPONIBLE | RESERVADA | VENDIDA | ALQUILADA"
    }
    CLIENTES {
        BIGINT id PK
        VARCHAR identificacion UK
        VARCHAR nombre
        VARCHAR apellido
        VARCHAR telefono
        VARCHAR correo
    }
    VISITAS {
        BIGINT id PK
        BIGINT id_cliente FK
        BIGINT id_propiedad FK
        BIGINT id_usuario FK
        DATE fecha
        TIME hora_inicio
        TIME hora_fin "hora_inicio + 60 min"
        VARCHAR estado "PENDIENTE | CANCELADA"
    }
    HISTORIAL_VISITAS {
        BIGINT id PK
        BIGINT id_visita FK
        BIGINT id_usuario_responsable FK
        VARCHAR tipo_cambio "CANCELACION | REPROGRAMACION"
        VARCHAR motivo
        DATETIME fecha_hora_cambio
        VARCHAR fecha_anterior "YYYY-MM-DD HH:MM"
        VARCHAR fecha_nueva "YYYY-MM-DD HH:MM"
    }
    AVISOS {
        BIGINT id PK
        BIGINT id_usuario FK
        VARCHAR mensaje
        DATETIME fecha_hora
    }
```

---

## 2. Arquitectura por Capas y Separación de Responsabilidades

```mermaid
flowchart TD
    UI["Interfaz / Vista (index.html, admin.html, asesor.html, panel.js)"] --> CTRL["2ª Familia: Controller (@RestController)\nCoordina peticiones HTTP y respuestas JSON"]
    CTRL --> SVC_I["3ª Familia: Service Interface\nDefine el contrato de operaciones"]
    SVC_I --> SVC_IMPL["4ª Familia: ServiceImpl (@Service, @Transactional)\nContiene el 100% de las Reglas de Negocio (RN-01 a RN-10)"]
    SVC_IMPL --> REPO["5ª Familia: Repository (Spring Data JPA JpaRepository)\nPersistencia y consultas parametrizadas"]
    REPO --> ENT["1ª Familia: Entities (@Entity JPA)\nRepresentan las tablas del DER"]
    ENT --> DB[("Base de Datos Relacional (H2 modo MySQL / MySQL)")]
```

### 2.1. Diagrama de Clases (Las 5 Familias del Diseño Aprobado)

```mermaid
classDiagram
    class PropiedadesController {
        -PropiedadService propiedadService
        +registrarPropiedad(dto, request)
        +cambiarEstado(id, body, request)
        +listar(estado)
        +listarDisponibles()
    }
    class ClientesController {
        -ClienteService clienteService
        +registrarCliente(cliente)
        +listarClientes()
        +buscarPorId(id)
    }
    class VisitasController {
        -VisitaService visitaService
        +agendarVisita(dto, request)
        +cancelarVisita(id, body, request)
        +reprogramarVisita(id, body, request)
        +listarPendientes(fecha, asesor, request)
        +consultarHistorial(id, request)
        +listarVisitas(cliente, propiedad, fecha, request)
        +obtenerHorariosLibres(fecha, propiedad, cliente, asesor, visitaIgnorada, request)
    }
    class PropiedadService {
        <<interface>>
        +registrarPropiedad(propiedad, usuarioSesion)
        +cambiarEstado(idPropiedad, nuevoEstado, usuarioSesion)
        +listarDisponibles()
        +listarTodas()
    }
    class VisitaService {
        <<interface>>
        +agendarVisita(dto, usuarioSesion)
        +cancelarVisita(idVisita, motivo, usuarioSesion)
        +reprogramarVisita(idVisita, nuevaFecha, nuevaHoraInicio, motivo, usuarioSesion)
        +cancelarVisitasPorPropiedad(idPropiedad, motivo, usuarioResponsable)
        +listarPendientes(fecha, idAsesor, usuarioSesion)
        +consultarHistorial(idVisita, usuarioSesion)
        +listarPorClienteOPropiedad(idCliente, idPropiedad, usuarioSesion)
        +generarAvisoInterno(visita, mensaje)
    }
    class PropiedadServiceImpl {
        -PropiedadRepository propiedadRepository
        -VisitaService visitaService
        -validarDatos(propiedad)
        -validarRol(usuarioSesion)
        -validarReglasEstado(modalidad, estadoActual, nuevoEstado, esCambio)
    }
    class VisitaServiceImpl {
        -VisitaRepository visitaRepository
        -PropiedadRepository propiedadRepository
        -ClienteRepository clienteRepository
        -UsuarioRepository usuarioRepository
        -HistorialVisitaRepository historialVisitaRepository
        -AvisoRepository avisoRepository
        -validarDisponibilidad(idPropiedad)
        -calcularHoraFin(horaInicio)
        -validarCruceHorario(...)
        -validarSinDuplicadoClientePropiedad(...)
        -validarAsesor(idAsesorDto, usuarioSesion)
        -registrarHistorial(...)
    }

    PropiedadesController --> PropiedadService
    VisitasController --> VisitaService
    PropiedadService <|.. PropiedadServiceImpl
    VisitaService <|.. VisitaServiceImpl
```

---

## 3. Reglas Estrictas del Estado de Reservación (`RESERVADA`) y Reprogramación sin Duplicados

1. **Coherencia estricta `Modalidad` vs `EstadoPropiedad` (`PropiedadServiceImpl.validarReglasEstado`)**:
   - Una propiedad en modalidad `VENTA` solo admite los estados `DISPONIBLE`, `RESERVADA` o `VENDIDA` (se rechaza `ALQUILADA` con `400 Bad Request`).
   - Una propiedad en modalidad `ALQUILER` solo admite los estados `DISPONIBLE`, `RESERVADA` o `ALQUILADA` (se rechaza `VENDIDA` con `400 Bad Request`).
   - Una propiedad que ya figura como `VENDIDA` o `ALQUILADA` no puede pasar directamente a `RESERVADA` sin antes habilitarse como `DISPONIBLE`.
2. **Bloqueo y Cancelación en Cascada al pasar a `RESERVADA` (`RN-01` y `RN-02`)**:
   - Al marcar una propiedad como `RESERVADA`, todas sus visitas futuras en estado `PENDIENTE` se cancelan automáticamente con el motivo `"propiedad reservada"`, registrando el cambio en `HistorialVisita` y enviando un `Aviso` interno al asesor asignado.
   - Mientras esté `RESERVADA`, el sistema rechaza agendar o reprogramar visitas sobre ella (`RN-01`) y devuelve `0` horarios disponibles en `/api/visitas/horarios-libres`.
3. **Prevención de Duplicados y Experiencia de Usuario en Reprogramación (`VisitaServiceImpl` + `panel.js`)**:
   - El sistema impide que un mismo cliente registre más de una visita `PENDIENTE` simultánea para la misma propiedad (`validarSinDuplicadoClientePropiedad`).
   - El sistema impide reprogramar una visita hacia su misma fecha y hora actual, evitando entradas duplicadas en el historial y en los avisos.
   - En la interfaz web (`admin.html` y `asesor.html`), la tarjeta de reprogramación permanece oculta hasta que el usuario pulsa **"Reprogramar"**, deshabilita el botón durante el envío para evitar dobles clics y se cierra automáticamente al confirmarse el cambio, resaltando en verde la fila actualizada.
