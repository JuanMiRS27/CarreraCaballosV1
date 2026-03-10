package com.example.carreracaballos.api;

import com.example.carreracaballos.game.SuitSpanish;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateGameRequest(
        @NotNull Long userId,
        @Min(3) @Max(30) int distance,
        @NotEmpty List<SuitSpanish> horses,
        @NotNull SuitSpanish selectedHorse,
        @Min(1) long betPoints
) {
}
