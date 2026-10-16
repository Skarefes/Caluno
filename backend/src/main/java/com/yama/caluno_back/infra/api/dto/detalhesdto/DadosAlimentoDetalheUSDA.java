package com.yama.caluno_back.infra.api.dto.detalhesdto;

import com.yama.caluno_back.infra.api.dto.DadosFoodPortionUSDA;

import java.util.List;

public record DadosAlimentoDetalheUSDA(
        Long fdcId,
        String description,
        String dataType,
        List<DadosNutrienteDetalheUSDA> foodNutrients,
        List<DadosFoodPortionUSDA> foodPortions
) {
}
