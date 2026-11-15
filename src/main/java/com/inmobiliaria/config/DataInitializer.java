package com.inmobiliaria.config;

import com.inmobiliaria.constants.EstadoPropiedad;
import com.inmobiliaria.constants.EstadoVisita;
import com.inmobiliaria.constants.Modalidad;
import com.inmobiliaria.constants.Rol;
import com.inmobiliaria.model.Cliente;
import com.inmobiliaria.model.Propiedad;
import com.inmobiliaria.model.Usuario;
import com.inmobiliaria.model.Visita;
import com.inmobiliaria.repository.ClienteRepository;
import com.inmobiliaria.repository.PropiedadRepository;
import com.inmobiliaria.repository.UsuarioRepository;
import com.inmobiliaria.repository.VisitaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Inicializador de datos semilla con contraseñas cifradas en BCrypt (Punto 9).
 * Si existen usuarios con contraseñas que no comiencen con "$2", las cifra automáticamente al arrancar.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PropiedadRepository propiedadRepository;
    private final ClienteRepository clienteRepository;
    private final VisitaRepository visitaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           PropiedadRepository propiedadRepository,
                           ClienteRepository clienteRepository,
                           VisitaRepository visitaRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.propiedadRepository = propiedadRepository;
        this.clienteRepository = clienteRepository;
        this.visitaRepository = visitaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Sembrar usuarios iniciales con BCrypt si la tabla está vacía
        if (usuarioRepository.count() == 0) {
            usuarioRepository.save(new Usuario(null, "Admin Principal", "admin", passwordEncoder.encode("admin123"), Rol.ADMINISTRADOR));
            usuarioRepository.save(new Usuario(null, "Carlos Asesor", "carlos", passwordEncoder.encode("carlos123"), Rol.ASESOR));
            usuarioRepository.save(new Usuario(null, "Laura Asesora", "laura", passwordEncoder.encode("laura123"), Rol.ASESOR));
        } else {
            // Punto 9: Si alguna clave en una base existente no está en formato BCrypt ($2...), cifrarla al arrancar
            for (Usuario u : usuarioRepository.findAll()) {
                if (u.getContrasena() != null && !u.getContrasena().startsWith("$2")) {
                    u.setContrasena(passwordEncoder.encode(u.getContrasena()));
                    usuarioRepository.save(u);
                }
            }
        }

        // 2. Sembrar catálogo de 10 propiedades iniciales con sus descripciones completas (Punto 4)
        List<Propiedad> catalogoInicial = List.of(
                new Propiedad(null, "P-01", "Av. Amazonas N34-120 y Núñez de Vela", "Casa", Modalidad.VENTA,
                        "Casa amplia de 3 dormitorios, 2 baños y medio, jardín posterior y garaje doble en zona comercial.", EstadoPropiedad.DISPONIBLE),
                new Propiedad(null, "P-02", "Av. Shyris N36-45 y Portugal", "Departamento", Modalidad.ALQUILER,
                        "Departamento moderno frente al parque La Carolina, 2 habitaciones, balcón panorámico y seguridad 24/7.", EstadoPropiedad.DISPONIBLE),
                new Propiedad(null, "P-03", "Av. 6 de Diciembre N39-88 y Eloy Alfaro", "Departamento", Modalidad.VENTA,
                        "Suite ejecutiva en piso 8 con acabados de porcelanato, cocina tipo americana, bodega y 1 parqueadero.", EstadoPropiedad.DISPONIBLE),
                new Propiedad(null, "P-04", "Urb. El Condado, Calle C y Paseo de los Eucaliptos", "Casa", Modalidad.VENTA,
                        "Residencia familiar de 2 plantas, 4 dormitorios con baño privado, área de BBQ, estudio y seguridad privada.", EstadoPropiedad.DISPONIBLE),
                new Propiedad(null, "P-05", "Av. González Suárez N27-142 y Orellana", "Departamento", Modalidad.ALQUILER,
                        "Departamento de lujo de 3 dormitorios con vista al valle, gimnasio comunal, terraza y 2 estacionamientos.", EstadoPropiedad.DISPONIBLE),
                new Propiedad(null, "P-06", "Valle de Cumbayá, Av. Interoceánica Km 11", "Casa", Modalidad.ALQUILER,
                        "Casa independiente en conjunto privado con piscina climatizada, 3 habitaciones, pérgola y áreas verdes.", EstadoPropiedad.DISPONIBLE),
                new Propiedad(null, "P-07", "Av. República del Salvador N35-204 y Moscú", "Departamento", Modalidad.VENTA,
                        "Departamento contemporáneo de 2 dormitorios en zona financiera, edificio inteligente con coworking y terraza.", EstadoPropiedad.DISPONIBLE),
                new Propiedad(null, "P-08", "Sector Ponceano Alto, Av. Real Audiencia N62-15", "Casa", Modalidad.VENTA,
                        "Casa esquinera de 3 pisos, 4 habitaciones, patio amplio, cuarto de máquinas y estacionamiento para 2 vehículos.", EstadoPropiedad.DISPONIBLE),
                new Propiedad(null, "P-09", "Av. De los Granados E14-90 y El Inca", "Casa", Modalidad.ALQUILER,
                        "Casa remodelada de 3 dormitorios dentro de urbanización cerrada, cerca de centros educativos y comerciales.", EstadoPropiedad.DISPONIBLE),
                new Propiedad(null, "P-10", "Av. Mariana de Jesús OE7-40 y Pradera", "Departamento", Modalidad.VENTA,
                        "Departamento iluminado de 3 habitaciones en 5to piso con ascensor, área de lavado y guardianía permanente.", EstadoPropiedad.RESERVADA)
        );

        for (Propiedad p : catalogoInicial) {
            Propiedad existente = propiedadRepository.findByCodigoInterno(p.getCodigoInterno()).orElse(null);
            if (existente == null) {
                propiedadRepository.save(p);
            } else if (existente.getDescripcion() == null || existente.getDescripcion().isBlank()) {
                existente.setDescripcion(p.getDescripcion());
                propiedadRepository.save(existente);
            }
        }

        // 3. Sembrar catálogo de 11 clientes iniciales de forma idempotente (3 originales + 8 adicionales)
        List<Cliente> clientesIniciales = List.of(
                new Cliente(null, "1723456789", "Grecco", "Cedeño", "0991234567", "grecco@ejemplo.com"),
                new Cliente(null, "1314567890", "Britany Tahis", "Macias", "0987654321", "britany@ejemplo.com"),
                new Cliente(null, "1719876543", "Mateo", "Andrade", "0998877665", "mateo.andrade@ejemplo.com"),
                new Cliente(null, "1720112233", "Valeria", "Domínguez", "0992345678", "valeria.dominguez@ejemplo.com"),
                new Cliente(null, "0923456781", "Santiago", "Villavicencio", "0983456789", "santiago.v@ejemplo.com"),
                new Cliente(null, "1715566778", "Camila", "Espinoza", "0994567890", "camila.espinoza@ejemplo.com"),
                new Cliente(null, "1804455667", "Andrés", "Peñaherrera", "0985678901", "andres.pena@ejemplo.com"),
                new Cliente(null, "1726677889", "Daniela", "Montalvo", "0996789012", "daniela.montalvo@ejemplo.com"),
                new Cliente(null, "0105566779", "Fernando", "Crespo", "0987890123", "fernando.crespo@ejemplo.com"),
                new Cliente(null, "1718899001", "Gabriela", "Zambrano", "0998901234", "gabriela.zambrano@ejemplo.com"),
                new Cliente(null, "1309988776", "Ricardo", "Alcívar", "0989012345", "ricardo.alcivar@ejemplo.com")
        );

        for (Cliente c : clientesIniciales) {
            if (clienteRepository.findByIdentificacion(c.getIdentificacion()).isEmpty()) {
                clienteRepository.save(c);
            }
        }

        // 4. Limpiar cualquier visita PENDIENTE duplicada existente para un mismo (cliente, propiedad)
        List<Visita> pendientesActuales = visitaRepository.findByEstadoOrderByFechaAscHoraInicioAsc(EstadoVisita.PENDIENTE);
        Set<String> clavesClientePropiedad = new HashSet<>();
        for (Visita v : pendientesActuales) {
            String clave = v.getIdCliente() + "-" + v.getIdPropiedad();
            if (!clavesClientePropiedad.add(clave)) {
                v.setEstado(EstadoVisita.CANCELADA);
                visitaRepository.save(v);
            }
        }

        // 5. Asegurar que cada asesor de prueba tenga una visita PENDIENTE futura en propiedades distintas (P-04 y P-05)
        Cliente cliente = clienteRepository.findAll().stream().findFirst().orElse(null);
        Propiedad propCarlos = propiedadRepository.findByCodigoInterno("P-04").orElse(null);
        Propiedad propLaura = propiedadRepository.findByCodigoInterno("P-05").orElse(null);
        Usuario carlos = usuarioRepository.findByUsuario("carlos").orElse(null);
        Usuario laura = usuarioRepository.findByUsuario("laura").orElse(null);

        LocalDate proximoDiaHabil = LocalDate.now().plusDays(1);
        while (proximoDiaHabil.getDayOfWeek() == DayOfWeek.SATURDAY || proximoDiaHabil.getDayOfWeek() == DayOfWeek.SUNDAY) {
            proximoDiaHabil = proximoDiaHabil.plusDays(1);
        }

        if (cliente != null && propCarlos != null && carlos != null) {
            boolean carlosTienePendiente = visitaRepository.findByEstadoOrderByFechaAscHoraInicioAsc(EstadoVisita.PENDIENTE)
                    .stream().anyMatch(v -> carlos.getId().equals(v.getIdUsuario()));
            if (!carlosTienePendiente && propCarlos.getEstado() == EstadoPropiedad.DISPONIBLE) {
                visitaRepository.save(new Visita(
                        cliente,
                        propCarlos,
                        carlos,
                        proximoDiaHabil,
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        EstadoVisita.PENDIENTE
                ));
            }
        }
        if (cliente != null && propLaura != null && laura != null) {
            boolean lauraTienePendiente = visitaRepository.findByEstadoOrderByFechaAscHoraInicioAsc(EstadoVisita.PENDIENTE)
                    .stream().anyMatch(v -> laura.getId().equals(v.getIdUsuario()));
            if (!lauraTienePendiente && propLaura.getEstado() == EstadoPropiedad.DISPONIBLE) {
                visitaRepository.save(new Visita(
                        cliente,
                        propLaura,
                        laura,
                        proximoDiaHabil,
                        LocalTime.of(11, 0),
                        LocalTime.of(12, 0),
                        EstadoVisita.PENDIENTE
                ));
            }
        }
    }
}
