package com.yama.caluno_back.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Alimento {
    private long id;
    private String nome;
    private double calorias;
    private double carboidratos;
    private double gorduras;

    public Alimento (){}

}
