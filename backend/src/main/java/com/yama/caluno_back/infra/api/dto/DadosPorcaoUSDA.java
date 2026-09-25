package com.yama.caluno_back.infra.api.dto;

public record DadosPorcaoUSDA(
        //Para saber
        Double quantidade,
        Double pesoGrama,
        //unidade mostra se e pedaço ou grama
        String unidadeTipo,
        //Aqui entraria dados extras,como o tamanho ou tipo da medida, porem atualmente será opcional
        String modifier
) {
}
