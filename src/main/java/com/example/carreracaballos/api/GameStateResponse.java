package com.example.carreracaballos.api;

import com.example.carreracaballos.game.SuitSpanish;
import com.example.carreracaballos.game.TurnResult;
import com.example.carreracaballos.model.GameStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GameStateResponse(
        UUID id,
        Long userId,
        String userName,
        String groupCode,
        int distance,
        int turn,
        List<SuitSpanish> horses,
        SuitSpanish selectedHorse,
        long betPoints,
        long payoutPoints,
        long playerPointsBalance,
        Map<SuitSpanish, Integer> positions,
        SuitSpanish winner,
        GameStatus status,
        List<GameParticipantResponse> participants,
        TurnResult lastTurn,
        List<TurnResult> history
) {
}
