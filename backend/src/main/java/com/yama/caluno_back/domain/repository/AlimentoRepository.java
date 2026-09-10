package com.yama.caluno_back.domain.repository;

import com.yama.caluno_back.domain.alimento.Alimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;

public interface AlimentoRepository extends JpaRepository<Alimento, Long> {
}
