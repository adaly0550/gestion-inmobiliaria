package com.inmobiliaria.repository;

import com.inmobiliaria.constants.Rol;
import com.inmobiliaria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Quinta Familia: Repository - UsuarioRepository (Spring Data JPA).
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);

    List<Usuario> findByRolOrderByNombresAsc(Rol rol);
}
