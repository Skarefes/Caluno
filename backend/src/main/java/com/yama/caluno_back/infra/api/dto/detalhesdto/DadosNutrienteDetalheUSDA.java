package com.yama.caluno_back.infra.api.dto.detalhesdto;

public record DadosNutrienteDetalheUSDA(
        DadosNutrienteInfoUSDA nutrient,
        Double amount
) {
}
