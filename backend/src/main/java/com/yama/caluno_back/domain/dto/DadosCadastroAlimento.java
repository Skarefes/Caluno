package com.yama.caluno_back.domain.dto;

public record DadosCadastroAlimento(
        String nome,
        double calorias,
        double carboidratos,
        double gorduras,
        double sodio,
        double fibras
) {
}
