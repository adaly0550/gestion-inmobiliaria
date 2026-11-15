# Reglas del Proyecto (`REGLAS.md`)

Este documento establece los **principios arquitectónicos, convenciones de código y restricciones técnicas/operativas** que rigen el desarrollo y mantenimiento del **Sistema de Gestión Inmobiliaria** en **Spring Boot + Spring Data JPA**.

---

## 1. Principios del Proyecto

### 1.1. Arquitectura por Capas y las 5 Familias de Clases (Bajo Acoplamiento)
El sistema está estructurado en **Java 21 + Spring Boot 3** siguiendo exactamente el diagrama de arquitectura por capas del documento de análisis y diseño:
1. **Primera Familia: Entities (`com.inmobiliaria.model`)**:
   - Representan las entidades del DER mapeadas con JPA/Hibernate (`@Entity`): `Cliente`, `Propiedad`, `Usuario`, `Visita`, `HistorialVisita` y `Aviso`.
   - `Usuario` protege el atributo `contrasena` con `@JsonProperty(access = WRITE_ONLY)` para que nunca se exponga en respuestas JSON (Punto 15).
2. **Segunda Familia: Controller (`com.inmobiliaria.controller`)**:
   - `PropiedadesController`, `ClientesController`, `VisitasController`, `AuthController`, `AuditoriaController` y `AvisoController`.
   - Reciben las peticiones HTTP (`GET`, `POST`, `PATCH`), delegan el trabajo a la **interface** de su `Service` inyectada por constructor (Punto 18) y no contienen lógica de negocio (Punto 14).
3. **Tercera Familia: Service Interface (`com.inmobiliaria.service`)**:
   - Contratos que declaran **qué** operaciones ofrece cada módulo (`PropiedadService`, `ClienteService`, `VisitaService`, `AuthService`, `AuditoriaService`, `AvisoService`) desacoplando la presentación de la implementación.
4. **Cuarta Familia: Implementación (`com.inmobiliaria.service.impl`)**:
   - Clases `@Service` (`PropiedadServiceImpl`, `ClienteServiceImpl`, `VisitaServiceImpl`, etc.) que aplican el **100% de las Reglas de Negocio (`RN-01` a `RN-11`)** dentro de transacciones (`@Transactional`).
5. **Quinta Familia: Repository (`com.inmobiliaria.repository`)**:
   - Interfaces que extienden `JpaRepository` de **Spring Data JPA** (`PropiedadRepository`, `ClienteRepository`, `UsuarioRepository`, `VisitaRepository`, `HistorialVisitaRepository`, `AvisoRepository`).

### 1.2. Integridad, Transaccionalidad y Trazabilidad Total
- Las operaciones de escritura (`agendarVisita`, `cancelarVisita`, `reprogramarVisita`, `cambiarEstado`) se ejecutan de forma atómica con `@Transactional` (Punto 26).
- Toda cancelación o reprogramación registra en `HistorialVisita` el usuario responsable, tipo de cambio (`CANCELACION` o `REPROGRAMACION`), motivo, fecha/hora exacta del cambio, `fechaAnterior` (`"YYYY-MM-DD HH:MM"`) y `fechaNueva` (`"YYYY-MM-DD HH:MM"`).

### 1.3. Consistencia en Contratos de API y Manejo de Errores
- Todas las excepciones de negocio utilizan `AppException(mensaje, status)` y son formateadas centralizadamente por `GlobalExceptionHandler` devolviendo siempre `{ error, mensaje }` con el código HTTP exacto (`400`, `401`, `403`, `404`, `500`) (Punto 17).

---

## 2. Restricciones del Proyecto

### 2.1. Restricciones de Base de Datos y Persistencia
1. **Prohibición de Borrado Físico (`RNF-03` y `RN-08`)**:
   - Ninguna visita se elimina de la base de datos. Al cancelar una visita únicamente cambia su estado a `CANCELADA`.
2. **Integridad Referencial y Unicidad**:
   - Llaves foráneas activas mediante JPA (`@ManyToOne` / `@JoinColumn`) entre `Visita`, `Cliente`, `Propiedad`, `Usuario`, `HistorialVisita` y `Aviso`.
   - Unicidad obligatoria en `Cliente.identificacion`, `Propiedad.codigoInterno` y `Usuario.usuario`.

### 2.2. Restricciones de Seguridad (`RNF-01`, `RNF-02`, `RN-06`, `RN-07`, `RN-09`)
1. **Sesión en Servidor No Falsificable (Punto 8)**:
   - El sistema utiliza sesiones HTTP de servidor con cookie `httpOnly` y `SameSite=Lax`. No se confía en encabezados enviados por el cliente. En cada petición `AuthInterceptor` carga el `Usuario` y su `Rol` directamente desde la base de datos.
2. **Contraseñas Cifradas con BCrypt (Punto 9)**:
   - Todas las contraseñas se almacenan y verifican con `BCryptPasswordEncoder`.
3. **Autenticación Obligatoria en Todos los Módulos (Punto 10 / `RN-09`)**:
   - Ningún endpoint de clientes, propiedades, visitas, auditoría o avisos es público; solo asesores y administradores autenticados acceden al sistema.
4. **Prevención de XSS en el Panel Web (Punto 11)**:
   - Todos los textos dinámicos renderizados en tablas pasan por `escapeHtml()` o `textContent`.

### 2.3. Restricciones Operativas y de Negocio
1. **Duración Fija de 60 Minutos y No Solapamiento (`RN-04`)**:
   - Toda visita dura exactamente **60 minutos** (`horaFin = horaInicio + 60 min`).
   - En reprogramaciones se excluye la propia visita (`v.id != idVisita`) al verificar cruces de horario.
2. **Asesor Asignado Válido (`RN-03`, `RN-07`, Punto 12)**:
   - Toda visita es atendida por un usuario con rol `ASESOR` validado con `UsuarioRepository`.
3. **Cancelación en Cascada Solo de Visitas Futuras (`RN-02`, Punto 7)**:
   - Cuando una propiedad pasa a `RESERVADA`, `VENDIDA` o `ALQUILADA`, solo se cancelan las visitas `PENDIENTE` cuya fecha y hora de inicio sean posteriores al momento actual.
4. **Restricciones de Control de Versiones (Punto 29)**:
   - Archivos locales generados (`target/`, `data/`, `.idea/`, `*.iml`, `.env`, `inmobiliaria.sqlite`) están excluidos en `.gitignore`.
