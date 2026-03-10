package com.example.carreracaballos.api;

import com.example.carreracaballos.game.SuitSpanish;
import com.example.carreracaballos.model.GameStatus;

import java.util.List;
import java.util.UUID;

public record ActiveGroupGameResponse(
        UUID id,
        int distance,
        int turn,
        List<SuitSpanish> horses,
        GameStatus status,
        int participantCount
) {
}
