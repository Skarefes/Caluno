package com.yama.caluno_back.infra.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenFoodFactsDTO(
        @JsonProperty("product_name")
        String productName,
        NutrimentsDTO nutriments
) {
}
