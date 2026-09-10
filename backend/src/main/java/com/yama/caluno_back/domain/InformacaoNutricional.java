package com.yama.caluno_back.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record InformacaoNutricional (
        double calorias,
        double carboidratos,
        double gorduras,
        double sodio,
        double fibras) {

}
