package com.yama.caluno_back.infra.api.dto;

import com.yama.caluno_back.domain.InformacaoNutricional;

import java.util.List;

public record DadosResultadoUSDA(
        Long fdcId,
        String nome,
        String dataType,
        InformacaoNutricional informacaoNutricional,
        List<DadosPorcaoUSDA> porcoes
) {
}
