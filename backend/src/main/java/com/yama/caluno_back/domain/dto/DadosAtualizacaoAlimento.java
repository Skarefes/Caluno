package com.yama.caluno_back.domain.dto;

public record DadosAtualizacaoAlimento(
        String nome,
        Double calorias,
        Double proteinas,
        Double carboidratos,
        Double gorduras,
        Double sodio,
        Double fibras
) {
}
