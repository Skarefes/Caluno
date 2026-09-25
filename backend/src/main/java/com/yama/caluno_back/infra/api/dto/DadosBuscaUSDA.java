package com.yama.caluno_back.infra.api.dto;

import java.util.List;

public record DadosBuscaUSDA(
        List<DadosAlimentoUSDA> foods
) {
}
