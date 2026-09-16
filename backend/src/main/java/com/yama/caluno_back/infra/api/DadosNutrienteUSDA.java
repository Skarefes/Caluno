package com.yama.caluno_back.infra.api;

import java.math.BigDecimal;

public record DadosNutrienteUSDA(
        Integer nutrientId,
        String nutrientName,
        String nutrientNumber,
        String unitName,
        Double value,
        String derivationCode,
        String derivationDescription
) {
}
