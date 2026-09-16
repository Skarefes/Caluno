package com.yama.caluno_back.domain.dto;

import com.yama.caluno_back.domain.alimento.Alimento;

import java.math.BigDecimal;

public record DadosDetalhamentoAlimento(
        Long id,
        String nome,
        Double calorias,
        Double proteinas,
        Double carboidratos,
        Double gorduras,
        Double sodio,
        Double fibras ) {

    //ligação para deixar a entidade do banco de dados e a saida limpa e centralizada
    public DadosDetalhamentoAlimento (Alimento alimento){
        this(alimento.getId(), alimento.getNome(),
                alimento.getInformacaoNutricional().getCalorias(),
                alimento.getInformacaoNutricional().getProteinas(),
                alimento.getInformacaoNutricional().getCarboidratos(),
                alimento.getInformacaoNutricional().getGorduras(),
                alimento.getInformacaoNutricional().getSodio(),
                alimento.getInformacaoNutricional().getFibras());

    }
}
