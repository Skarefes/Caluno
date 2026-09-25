package com.yama.caluno_back.domain.dto;

import com.yama.caluno_back.domain.InformacaoNutricional;
import com.yama.caluno_back.domain.registro_consumo.RegistroConsumo;

import java.time.LocalDateTime;

public record DadosDetalhamentoConsumo(
        Long id,
        String nomeAlimento,
        Double quantidade,
        String unidadeTipo,
        Double pesoGrama,
        InformacaoNutricional informacao,
        LocalDateTime data
) {
    public DadosDetalhamentoConsumo(RegistroConsumo registro, InformacaoNutricional informacao){
        this(
                registro.getId(),
                registro.getAlimento().getNome(),
                registro.getQuantidade(),
                registro.getUnidadeTipo(),
                registro.getPesoGrama(),
                informacao,
                registro.getData()
        );
    }
}
