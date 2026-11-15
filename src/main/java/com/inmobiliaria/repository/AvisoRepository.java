package com.inmobiliaria.repository;

import com.inmobiliaria.model.Aviso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Quinta Familia: Repository - AvisoRepository (Spring Data JPA).
 */
@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    List<Aviso> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    List<Aviso> findAllByOrderByIdDesc();
}
