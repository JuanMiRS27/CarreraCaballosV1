package com.example.carreracaballos.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PurchasePointsRequest(
        @NotNull Long userId,
        @Min(1) int packageCount
) {
}
