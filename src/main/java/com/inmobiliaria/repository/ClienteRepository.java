package com.inmobiliaria.repository;

import com.inmobiliaria.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Quinta Familia: Repository - ClienteRepository (Spring Data JPA).
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdentificacion(String identificacion);

    List<Cliente> findAllByOrderByIdDesc();
}
