package com.yama.caluno_back.domain.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

    //Aqui vai saber se o nome existe no banco ou não
    boolean existsByNome(String nome);
    //Como é optional o valor pode ser null
    Optional<Usuario> findByNome(String nome);
}
