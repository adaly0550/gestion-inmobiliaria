package com.inmobiliaria.repository;

import com.inmobiliaria.model.HistorialVisita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Quinta Familia: Repository - HistorialVisitaRepository (Spring Data JPA).
 */
@Repository
public interface HistorialVisitaRepository extends JpaRepository<HistorialVisita, Long> {

    List<HistorialVisita> findByVisitaIdOrderByIdDesc(Long visitaId);

    default List<HistorialVisita> findByVisitaId(Long visitaId) {
        return findByVisitaIdOrderByIdDesc(visitaId);
    }

    List<HistorialVisita> findAllByOrderByIdDesc();
}
