package com.example.carreracaballos.service;

import com.example.carreracaballos.api.GameStateResponse;
import com.example.carreracaballos.game.SuitSpanish;
import com.example.carreracaballos.model.GameStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RaceGameServiceTest {

    @Autowired
    private RaceGameService service;

    @Autowired
    private UserAccountService userAccountService;

    private Long userId;
    private Long secondUserId;

    @BeforeEach
    void setUp() {
        String email = "test" + System.nanoTime() + "@mail.com";
        userId = userAccountService.register("Tester", email, "1234").userId();
        String secondEmail = "test" + (System.nanoTime() + 1) + "@mail.com";
        secondUserId = userAccountService.register("Tester 2", secondEmail, "1234").userId();
    }

    @Test
    void shouldRejectInvalidHorseCount() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createGame(userId, 8, List.of(SuitSpanish.ORO), SuitSpanish.ORO, 100));
    }

    @Test
    void shouldRejectDuplicateHorses() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createGame(userId, 8, List.of(SuitSpanish.ORO, SuitSpanish.ORO), SuitSpanish.ORO, 100));
    }

    @Test
    void shouldFinishRaceWithWinner() {
        GameStateResponse game = service.createGame(
                userId,
                3,
                List.of(SuitSpanish.ORO, SuitSpanish.COPA),
                SuitSpanish.ORO,
                100
        );
        GameStateResponse state = game;
        int guard = 0;

        while (state.winner() == null && guard < 200) {
            state = service.step(game.id(), userId);
            guard += 1;
        }

        assertTrue(guard < 200, "La carrera debio finalizar antes.");
        assertNotNull(state.winner(), "Debe existir ganador.");
        assertEquals(GameStatus.FINISHED, state.status());
    }

    @Test
    void shouldAllowAnotherUserInSameGroupToJoinActiveRace() {
        GameStateResponse firstBet = service.createGame(
                userId,
                5,
                List.of(SuitSpanish.ORO, SuitSpanish.COPA, SuitSpanish.ESPADA),
                SuitSpanish.ORO,
                100
        );

        GameStateResponse secondBet = service.createGame(
                secondUserId,
                5,
                List.of(SuitSpanish.ORO, SuitSpanish.COPA, SuitSpanish.ESPADA),
                SuitSpanish.COPA,
                150
        );

        assertEquals(firstBet.id(), secondBet.id());
        assertEquals(2, secondBet.participants().size());
        assertEquals(SuitSpanish.COPA, secondBet.selectedHorse());
        assertEquals(150, secondBet.betPoints());
    }
}
