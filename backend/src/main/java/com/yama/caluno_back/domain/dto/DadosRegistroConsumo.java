package com.yama.caluno_back.domain.dto;

public record DadosRegistroConsumo(
        Long alimentoId,
        Double quantidade,
        String unidadeTipo //Aqui entra como grama ou como unidade
) {
}
