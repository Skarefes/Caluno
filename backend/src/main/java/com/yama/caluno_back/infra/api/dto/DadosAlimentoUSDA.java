package com.yama.caluno_back.infra.api.dto;

import java.util.List;

public record DadosAlimentoUSDA(
        Long fdcId,
        String description,
        String dataType,
        List<DadosNutrienteUSDA> foodNutrients,
        List<DadosFoodPortionUSDA> foodPortions
) {
}
