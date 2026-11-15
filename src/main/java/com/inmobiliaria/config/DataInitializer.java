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

        // 2. Sembrar propiedades iniciales con descripción (Punto 4)
        if (propiedadRepository.count() == 0) {
            propiedadRepository.save(new Propiedad(
                    null,
                    "P-01",
                    "Av. Amazonas",
                    "Casa",
                    Modalidad.VENTA,
                    "Casa amplia de 3 dormitorios con jardín y garaje en zona comercial.",
                    EstadoPropiedad.DISPONIBLE
            ));
            propiedadRepository.save(new Propiedad(
                    null,
                    "P-02",
                    "Av. Shyris y Portugal",
                    "Departamento",
                    Modalidad.ALQUILER,
                    "Departamento moderno frente al parque La Carolina, 2 habitaciones.",
                    EstadoPropiedad.DISPONIBLE
            ));
        }

        // 3. Sembrar cliente inicial de prueba
        if (clienteRepository.count() == 0) {
            clienteRepository.save(new Cliente(
                    null,
                    "1723456789",
                    "Grecco",
                    "Cedeño",
                    "0991234567",
                    "grecco@ejemplo.com"
            ));
        }

        // 4. Asegurar que cada asesor de prueba tenga al menos una visita PENDIENTE futura para probar reprogramación/cancelación
        Cliente cliente = clienteRepository.findAll().stream().findFirst().orElse(null);
        Propiedad prop1 = propiedadRepository.findByCodigoInterno("P-01").orElse(null);
        Propiedad prop2 = propiedadRepository.findByCodigoInterno("P-02").orElse(null);
        Usuario carlos = usuarioRepository.findByUsuario("carlos").orElse(null);
        Usuario laura = usuarioRepository.findByUsuario("laura").orElse(null);

        LocalDate proximoDiaHabil = LocalDate.now().plusDays(1);
        while (proximoDiaHabil.getDayOfWeek() == DayOfWeek.SATURDAY || proximoDiaHabil.getDayOfWeek() == DayOfWeek.SUNDAY) {
            proximoDiaHabil = proximoDiaHabil.plusDays(1);
        }

        if (cliente != null && prop1 != null && carlos != null) {
            boolean carlosTienePendiente = visitaRepository.findByEstadoOrderByFechaAscHoraInicioAsc(EstadoVisita.PENDIENTE)
                    .stream().anyMatch(v -> carlos.getId().equals(v.getIdUsuario()));
            if (!carlosTienePendiente && prop1.getEstado() == EstadoPropiedad.DISPONIBLE) {
                visitaRepository.save(new Visita(
                        cliente,
                        prop1,
                        carlos,
                        proximoDiaHabil,
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        EstadoVisita.PENDIENTE
                ));
            }
        }
        if (cliente != null && prop2 != null && laura != null) {
            boolean lauraTienePendiente = visitaRepository.findByEstadoOrderByFechaAscHoraInicioAsc(EstadoVisita.PENDIENTE)
                    .stream().anyMatch(v -> laura.getId().equals(v.getIdUsuario()));
            if (!lauraTienePendiente && prop2.getEstado() == EstadoPropiedad.DISPONIBLE) {
                visitaRepository.save(new Visita(
                        cliente,
                        prop2,
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
