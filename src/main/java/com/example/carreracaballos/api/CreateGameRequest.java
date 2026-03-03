package com.example.carreracaballos.api;

import com.example.carreracaballos.game.SuitSpanish;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateGameRequest(
        @Min(3) @Max(30) int distance,
        @NotEmpty List<SuitSpanish> horses
) {
}
