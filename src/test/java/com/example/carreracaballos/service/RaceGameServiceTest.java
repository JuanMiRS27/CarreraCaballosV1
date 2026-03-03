package com.example.carreracaballos.service;

import com.example.carreracaballos.api.GameStateResponse;
import com.example.carreracaballos.game.SuitSpanish;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaceGameServiceTest {

    private final RaceGameService service = new RaceGameService();

    @Test
    void shouldRejectInvalidHorseCount() {
        assertThrows(IllegalArgumentException.class, () -> service.createGame(8, List.of(SuitSpanish.ORO)));
    }

    @Test
    void shouldRejectDuplicateHorses() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createGame(8, List.of(SuitSpanish.ORO, SuitSpanish.ORO)));
    }

    @Test
    void shouldFinishRaceWithWinner() {
        GameStateResponse game = service.createGame(3, List.of(SuitSpanish.ORO, SuitSpanish.COPA));
        GameStateResponse state = game;
        int guard = 0;

        while (state.winner() == null && guard < 200) {
            state = service.step(game.id());
            guard += 1;
        }

        assertTrue(guard < 200, "La carrera debio finalizar antes.");
        assertNotNull(state.winner(), "Debe existir ganador.");
    }
}
