package com.yama.caluno_back.domain.traducao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TraducaoRepository extends JpaRepository<Traducao, Long> {
    Optional<Traducao> findByOrigemAndIdiomaOrigemAndIdiomaDestino(String origem,
                                                                   String idiomaOrigem,
                                                                   String idiomaDestino);
}
