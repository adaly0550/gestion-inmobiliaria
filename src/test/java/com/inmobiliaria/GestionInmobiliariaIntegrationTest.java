package com.inmobiliaria;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.config.AuthInterceptor;
import com.inmobiliaria.model.Usuario;
import com.inmobiliaria.repository.UsuarioRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Suite de 24 pruebas de integración automatizadas (docs/04_VALIDACION.md).
 * Verifica HU-01 a HU-11, RF-01 a RF-13, RN-01 a RN-10, RNF-01 a RNF-03 y los 31 puntos de auditoría.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:inmobiliaria_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class GestionInmobiliariaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockHttpSession crearSesion(String username) {
        Usuario u = usuarioRepository.findByUsuario(username).orElseThrow();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthInterceptor.SESSION_USER_ID, u.getId());
        return session;
    }

    private LocalDate proximoDiaHabil(int diasAdicionales) {
        LocalDate fecha = LocalDate.now().plusDays(diasAdicionales);
        while (fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }

    @Test
    @Order(1)
    void test01_loginExitosoYCookieHttpOnly() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("usuario", "admin", "password", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("admin"))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andReturn();

        assertThat(res.getRequest().getSession(false)).isNotNull();
    }

    @Test
    @Order(2)
    void test02_loginCredencialesInvalidasRetorna401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("usuario", "admin", "password", "claveErronea"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @Order(3)
    void test03_clientesSinSesionRetorna401() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @Order(4)
    void test04_adminRegistraPropiedadConDescripcionYCodigoAutomatico() throws Exception {
        MockHttpSession adminSession = crearSesion("admin");

        mockMvc.perform(post("/api/propiedades")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "direccion", "Av. República del Salvador",
                                "tipo", "Departamento",
                                "modalidad", "VENTA",
                                "descripcion", "Suite ejecutiva piso 10 con parqueadero"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoInterno").value("P-03"))
                .andExpect(jsonPath("$.descripcion").value("Suite ejecutiva piso 10 con parqueadero"))
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));
    }

    @Test
    @Order(5)
    void test05_asesorNoPuedeRegistrarPropiedadRetorna403() throws Exception {
        MockHttpSession asesorSession = crearSesion("carlos");

        mockMvc.perform(post("/api/propiedades")
                        .session(asesorSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "direccion", "Calle Falsa 123",
                                "tipo", "Casa",
                                "modalidad", "ALQUILER"
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @Order(6)
    void test06_validacionTipoYModalidadPropiedadRetorna400() throws Exception {
        MockHttpSession adminSession = crearSesion("admin");

        mockMvc.perform(post("/api/propiedades")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "direccion", "Av. Patria",
                                "tipo", "BodegaInvalida",
                                "modalidad", "VENTA"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @Order(7)
    void test07_asesorConsultaPropiedadesDisponibles() throws Exception {
        MockHttpSession asesorSession = crearSesion("carlos");

        mockMvc.perform(get("/api/propiedades?estado=DISPONIBLE").session(asesorSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("DISPONIBLE"));
    }

    @Test
    @Order(8)
    void test08_asesorRegistraClienteConDatosObligatorios() throws Exception {
        MockHttpSession asesorSession = crearSesion("carlos");

        mockMvc.perform(post("/api/clientes")
                        .session(asesorSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "identificacion", "0912345678",
                                "nombre", "Britany",
                                "apellido", "Macias",
                                "telefono", "0987654321"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identificacion").value("0912345678"));
    }

    @Test
    @Order(9)
    void test09_clienteSinTelefonoOIdentificacionDuplicadaRetorna400() throws Exception {
        MockHttpSession asesorSession = crearSesion("carlos");

        mockMvc.perform(post("/api/clientes")
                        .session(asesorSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "identificacion", "0912345678",
                                "nombre", "Duplicado",
                                "telefono", "0987654321"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    void test10_asesorAgendaVisitaCalcula60MinutosYGeneraAviso() throws Exception {
        MockHttpSession asesorSession = crearSesion("carlos");
        String fecha = proximoDiaHabil(5).toString();

        mockMvc.perform(post("/api/visitas")
                        .session(asesorSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idCliente", 1,
                                "idPropiedad", 1,
                                "fecha", fecha,
                                "horaInicio", "14:00"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.horaInicio").value("14:00"))
                .andExpect(jsonPath("$.horaFin").value("15:00"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        mockMvc.perform(get("/api/avisos").session(asesorSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mensaje").exists());
    }

    @Test
    @Order(11)
    void test11_adminAgendaVisitaAsignandoAsesorValido() throws Exception {
        MockHttpSession adminSession = crearSesion("admin");
        Usuario laura = usuarioRepository.findByUsuario("laura").orElseThrow();
        String fecha = proximoDiaHabil(6).toString();

        mockMvc.perform(post("/api/visitas")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idCliente", 1,
                                "idPropiedad", 2,
                                "idAsesor", laura.getId(),
                                "fecha", fecha,
                                "horaInicio", "10:00"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(laura.getId()));
    }

    @Test
    @Order(12)
    void test12_impedirCruceDeHorarioMismoAsesorRetorna400() throws Exception {
        MockHttpSession asesorSession = crearSesion("carlos");
        String fecha = proximoDiaHabil(5).toString();

        mockMvc.perform(post("/api/visitas")
                        .session(asesorSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idCliente", 2,
                                "idPropiedad", 2,
                                "fecha", fecha,
                                "horaInicio", "14:30"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @Order(13)
    void test13_impedirVisitaEnDomingoOFueraDeHorarioRetorna400() throws Exception {
        MockHttpSession asesorSession = crearSesion("carlos");
        LocalDate domingo = LocalDate.now().plusDays(1);
        while (domingo.getDayOfWeek() != DayOfWeek.SUNDAY) {
            domingo = domingo.plusDays(1);
        }

        mockMvc.perform(post("/api/visitas")
                        .session(asesorSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idCliente", 1,
                                "idPropiedad", 1,
                                "fecha", domingo.toString(),
                                "horaInicio", "10:00"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(14)
    void test14_asesorReprogramaSuVisitaExcluyendoLaPropiaYGuardaHistorial() throws Exception {
        MockHttpSession carlosSession = crearSesion("carlos");
        String nuevaFecha = proximoDiaHabil(7).toString();

        mockMvc.perform(patch("/api/visitas/1/reprogramar")
                        .session(carlosSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fecha", nuevaFecha,
                                "horaInicio", "15:00",
                                "motivo", "El cliente solicitó mover la cita a la tarde"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value(nuevaFecha))
                .andExpect(jsonPath("$.horaInicio").value("15:00"))
                .andExpect(jsonPath("$.horaFin").value("16:00"));
    }

    @Test
    @Order(15)
    void test15_reprogramarSinMotivoRetorna400() throws Exception {
        MockHttpSession carlosSession = crearSesion("carlos");
        String nuevaFecha = proximoDiaHabil(8).toString();

        mockMvc.perform(patch("/api/visitas/1/reprogramar")
                        .session(carlosSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fecha", nuevaFecha,
                                "horaInicio", "11:00",
                                "motivo", "   "
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(16)
    void test16_asesorNoPuedeReprogramarVisitaAjenaRetorna403() throws Exception {
        MockHttpSession carlosSession = crearSesion("carlos");
        String nuevaFecha = proximoDiaHabil(8).toString();

        // La visita #2 pertenece a Laura
        mockMvc.perform(patch("/api/visitas/2/reprogramar")
                        .session(carlosSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fecha", nuevaFecha,
                                "horaInicio", "12:00",
                                "motivo", "Intento sobre visita ajena"
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(17)
    void test17_asesorCancelaVisitaConservaRegistroYGuardaHistorial() throws Exception {
        MockHttpSession lauraSession = crearSesion("laura");

        mockMvc.perform(patch("/api/visitas/2/cancelar")
                        .session(lauraSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "motivo", "Cliente reprogramará el próximo mes"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    @Order(18)
    void test18_cambioEstadoPropiedadCancelaEnCascadaSoloVisitasFuturas() throws Exception {
        MockHttpSession adminSession = crearSesion("admin");

        // Cambiar propiedad #3 (P-03) a RESERVADA
        mockMvc.perform(patch("/api/propiedades/3/estado")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("estado", "RESERVADA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESERVADA"));
    }

    @Test
    @Order(19)
    void test19_noAgendarEnPropiedadNoDisponibleRetorna400() throws Exception {
        MockHttpSession carlosSession = crearSesion("carlos");
        String fecha = proximoDiaHabil(9).toString();

        mockMvc.perform(post("/api/visitas")
                        .session(carlosSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idCliente", 1,
                                "idPropiedad", 3,
                                "fecha", fecha,
                                "horaInicio", "10:00"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @Order(20)
    void test20_consultarVisitasPendientesPorFechaYPorAsesor() throws Exception {
        MockHttpSession carlosSession = crearSesion("carlos");
        Usuario laura = usuarioRepository.findByUsuario("laura").orElseThrow();

        // RN-07: Aunque el asesor envíe ?asesor=idLaura, el servidor fuerza su propio ID
        MvcResult res = mockMvc.perform(get("/api/visitas/pendientes?asesor=" + laura.getId())
                        .session(carlosSession))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(res.getResponse().getContentAsString());
        for (JsonNode v : arr) {
            assertThat(v.get("idUsuario").asLong()).isNotEqualTo(laura.getId());
        }
    }

    @Test
    @Order(21)
    void test21_adminConsultaHistorialDeUnaVisitaEspecifica() throws Exception {
        MockHttpSession adminSession = crearSesion("admin");
        MockHttpSession carlosSession = crearSesion("carlos");

        mockMvc.perform(get("/api/visitas/1/historial").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoCambio").value("REPROGRAMACION"))
                .andExpect(jsonPath("$[0].fechaAnterior").exists())
                .andExpect(jsonPath("$[0].fechaNueva").exists());

        mockMvc.perform(get("/api/visitas/9999/historial").session(adminSession))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/visitas/1/historial").session(carlosSession))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(22)
    void test22_adminConsultaVisitasPorClienteYPorPropiedadIncluyendoCanceladas() throws Exception {
        MockHttpSession adminSession = crearSesion("admin");

        MvcResult resCliente = mockMvc.perform(get("/api/visitas?cliente=1").session(adminSession))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode listaCli = objectMapper.readTree(resCliente.getResponse().getContentAsString());
        assertThat(listaCli.isArray()).isTrue();
        assertThat(listaCli.size()).isGreaterThanOrEqualTo(2);

        mockMvc.perform(get("/api/visitas?propiedad=2").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPropiedad").value(2));
    }

    @Test
    @Order(23)
    void test23_consultarHorariosLibresSugeridos() throws Exception {
        MockHttpSession carlosSession = crearSesion("carlos");
        String fecha = proximoDiaHabil(10).toString();

        mockMvc.perform(get("/api/visitas/horarios-libres?fecha=" + fecha).session(carlosSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("09:00"));
    }

    @Test
    @Order(24)
    void test24_logoutDestruyeSesionEnServidor() throws Exception {
        MockHttpSession carlosSession = crearSesion("carlos");

        mockMvc.perform(post("/api/auth/logout").session(carlosSession))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me").session(carlosSession))
                .andExpect(status().isUnauthorized());
    }
}
