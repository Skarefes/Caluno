package com.yama.caluno_back.domain.alimento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlimentoRepository extends JpaRepository<Alimento, Long> {

    Optional<Alimento> findByFdcId(Long fdcId);
}
