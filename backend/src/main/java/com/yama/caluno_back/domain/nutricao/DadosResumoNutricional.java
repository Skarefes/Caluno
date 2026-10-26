package com.yama.caluno_back.domain.nutricao;

import java.time.LocalDate;

public record DadosResumoNutricional(
        LocalDate data,
        Double calorias,
        Double proteinas,
        Double carboidratos,
        Double gorduras,
        Double sodio,
        Double fibras
) {
}
