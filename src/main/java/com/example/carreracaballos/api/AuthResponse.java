package com.example.carreracaballos.api;

public record AuthResponse(
        Long userId,
        String name,
        String email,
        long pointsBalance,
        GroupSummaryResponse group
) {
}
