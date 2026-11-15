package com.inmobiliaria.repository;

import com.inmobiliaria.constants.EstadoPropiedad;
import com.inmobiliaria.model.Propiedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Quinta Familia: Repository - PropiedadRepository (Spring Data JPA).
 */
@Repository
public interface PropiedadRepository extends JpaRepository<Propiedad, Long> {

    List<Propiedad> findByEstadoOrderByIdDesc(EstadoPropiedad estado);

    default List<Propiedad> findByEstado(EstadoPropiedad estado) {
        return findByEstadoOrderByIdDesc(estado);
    }

    Optional<Propiedad> findByCodigoInterno(String codigoInterno);

    List<Propiedad> findAllByOrderByIdDesc();
}
