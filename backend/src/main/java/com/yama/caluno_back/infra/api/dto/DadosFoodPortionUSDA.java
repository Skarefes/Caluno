package com.yama.caluno_back.infra.api.dto;

public record DadosFoodPortionUSDA(Double amount,
                                   Double gramWeight,
                                   String modifier,
                                   DadosMeasureUnitUSDA measureUnit) {
}
//Record que pega os dados do JSON cru, para serem lidos