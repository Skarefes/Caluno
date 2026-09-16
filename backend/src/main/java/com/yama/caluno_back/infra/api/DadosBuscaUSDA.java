package com.yama.caluno_back.infra.api;

import java.util.List;

public record DadosBuscaUSDA(
        List<DadosAlimentoUSDA> foods
) {
}
