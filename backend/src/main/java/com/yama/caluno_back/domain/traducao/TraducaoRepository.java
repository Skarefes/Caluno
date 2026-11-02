package com.yama.caluno_back.domain.traducao;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TraducaoRepository extends JpaRepository<Traducao, Long> {
    Optional<Traducao> findByOrigemAndIdiomaOrigemAndIdiomaDestino(String origem,
                                                                   String idiomaOrigem,
                                                                   String idiomaDestino);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO traducao (origem, idioma_origem, idioma_destino, traducao)
        VALUES (:origem, :idiomaOrigem, :idiomaDestino, :traducao)
        ON CONFLICT (origem, idioma_origem, idioma_destino) DO NOTHING
            """, nativeQuery = true)
    void inserirSeNaoExistir(@Param("origem") String origem,
                             @Param("idiomaOrigem") String idiomaOrigem,
                             @Param("idiomaDestino") String idiomaDestino,
                             @Param("traducao") String traducao);

}
