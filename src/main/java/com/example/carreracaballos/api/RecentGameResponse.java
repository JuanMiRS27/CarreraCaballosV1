package com.example.carreracaballos.api;

import com.example.carreracaballos.game.SuitSpanish;
import com.example.carreracaballos.model.GameStatus;

import java.time.Instant;
import java.util.UUID;

public record RecentGameResponse(
        UUID id,
        SuitSpanish selectedHorse,
        SuitSpanish winnerHorse,
        long betPoints,
        long payoutPoints,
        GameStatus status,
        Instant createdAt
) {
}
