package com.yama.caluno_back.domain.dto;

import com.yama.caluno_back.domain.alimento.Alimento;

public record DadosDetalhamentoAlimento(
        Long id,
        String nome,
        double calorias,
        double carboidratos,
        double gorduras,
        double sodio,
        double fibras ) {

    //ligação para deixar a entidade do banco de dados e a saida limpa e centralizada
    public DadosDetalhamentoAlimento (Alimento alimento){
        this(alimento.getId(), alimento.getNome(),
                alimento.getInformacaoNutricional().calorias(),
                alimento.getInformacaoNutricional().carboidratos(),
                alimento.getInformacaoNutricional().gorduras(),
                alimento.getInformacaoNutricional().sodio(),
                alimento.getInformacaoNutricional().fibras());

    }
}
