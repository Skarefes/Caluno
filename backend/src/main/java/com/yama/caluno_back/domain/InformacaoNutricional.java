package com.yama.caluno_back.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InformacaoNutricional {
    private Double calorias;
    private Double proteinas;
    private Double carboidratos;
    private Double gorduras;
    private Double sodio;
    private Double fibras;

}
