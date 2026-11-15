package com.inmobiliaria.repository;

import com.inmobiliaria.constants.EstadoVisita;
import com.inmobiliaria.model.Visita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Quinta Familia: Repository - VisitaRepository (Spring Data JPA).
 * Define exactamente los métodos del diagrama de arquitectura por capas.
 */
@Repository
public interface VisitaRepository extends JpaRepository<Visita, Long> {

    List<Visita> findByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);

    List<Visita> findByClienteIdAndFecha(Long clienteId, LocalDate fecha);

    List<Visita> findByPropiedadIdAndFecha(Long propiedadId, LocalDate fecha);

    List<Visita> findByPropiedadIdAndEstado(Long propiedadId, EstadoVisita estado);

    List<Visita> findByClienteIdOrderByIdDesc(Long clienteId);

    default List<Visita> findByClienteId(Long clienteId) {
        return findByClienteIdOrderByIdDesc(clienteId);
    }

    List<Visita> findByPropiedadIdOrderByIdDesc(Long propiedadId);

    default List<Visita> findByPropiedadId(Long propiedadId) {
        return findByPropiedadIdOrderByIdDesc(propiedadId);
    }

    List<Visita> findByEstadoOrderByFechaAscHoraInicioAsc(EstadoVisita estado);

    List<Visita> findAllByOrderByIdDesc();
}
