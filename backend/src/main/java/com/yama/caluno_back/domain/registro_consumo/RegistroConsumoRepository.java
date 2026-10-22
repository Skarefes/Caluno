package com.yama.caluno_back.domain.registro_consumo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistroConsumoRepository extends JpaRepository<RegistroConsumo, Long> {
    List<RegistroConsumo> findByUsuarioNome(String nome);
    Optional<RegistroConsumo> findByIdAndUsuarioNome(Long id, String nome);
}
