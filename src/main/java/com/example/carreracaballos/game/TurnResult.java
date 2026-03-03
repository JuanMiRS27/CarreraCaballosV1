package com.example.carreracaballos.game;

public record TurnResult(
        int turn,
        SpanishCard card,
        boolean horseMoved,
        SuitSpanish movedHorse,
        int newPosition,
        boolean penaltyTriggered,
        SpanishCard penaltyCard,
        boolean penaltyApplied,
        SuitSpanish penalizedHorse,
        int penalizedNewPosition,
        SuitSpanish winner
) {
}
