package com.example.carreracaballos.api;

import com.example.carreracaballos.game.SuitSpanish;

public record GameParticipantResponse(
        Long userId,
        String userName,
        SuitSpanish selectedHorse,
        long betPoints,
        long payoutPoints,
        boolean currentUser
) {
}
