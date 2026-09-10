package com.yama.caluno_back.infra.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NutrimentsDTO(
        @JsonProperty("energy-kcal_100g")
        Double energyKcal100g,

        @JsonProperty("proteins_100g")
        Double proteins100g,

        @JsonProperty("carbohydrates_100g")
        Double carbohydrates100g,

        @JsonProperty("fat_100g")
        Double fat100g,

        @JsonProperty("sodium_100g")
        Double sodium100g
) {
}
