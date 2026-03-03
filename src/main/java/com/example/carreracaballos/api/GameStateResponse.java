package com.example.carreracaballos.api;

import com.example.carreracaballos.game.SuitSpanish;
import com.example.carreracaballos.game.TurnResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GameStateResponse(
        UUID id,
        int distance,
        int turn,
        List<SuitSpanish> horses,
        Map<SuitSpanish, Integer> positions,
        SuitSpanish winner,
        TurnResult lastTurn,
        List<TurnResult> history
) {
}
