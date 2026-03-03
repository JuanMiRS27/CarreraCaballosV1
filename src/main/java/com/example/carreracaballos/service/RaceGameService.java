package com.example.carreracaballos.service;

import com.example.carreracaballos.api.GameStateResponse;
import com.example.carreracaballos.game.RaceGame;
import com.example.carreracaballos.game.SuitSpanish;
import com.example.carreracaballos.game.TurnResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RaceGameService {
    private final Map<UUID, RaceGame> games = new ConcurrentHashMap<>();

    public GameStateResponse createGame(int distance, List<SuitSpanish> selectedHorses) {
        if (distance < 3 || distance > 30) {
            throw new IllegalArgumentException("La distancia debe estar entre 3 y 30.");
        }
        if (selectedHorses == null || selectedHorses.size() < 2 || selectedHorses.size() > 4) {
            throw new IllegalArgumentException("Debe elegir entre 2 y 4 caballos.");
        }

        LinkedHashSet<SuitSpanish> deduplicated = new LinkedHashSet<>(selectedHorses);
        if (deduplicated.size() != selectedHorses.size()) {
            throw new IllegalArgumentException("No se permiten palos repetidos.");
        }

        UUID id = UUID.randomUUID();
        RaceGame game = new RaceGame(id, distance, new ArrayList<>(deduplicated));
        games.put(id, game);
        return toResponse(game, null);
    }

    public GameStateResponse getGame(UUID id) {
        RaceGame game = getRequiredGame(id);
        return toResponse(game, null);
    }

    public GameStateResponse step(UUID id) {
        RaceGame game = getRequiredGame(id);
        TurnResult lastTurn = game.step();
        return toResponse(game, lastTurn);
    }

    private RaceGame getRequiredGame(UUID id) {
        RaceGame game = games.get(id);
        if (game == null) {
            throw new IllegalArgumentException("La carrera no existe.");
        }
        return game;
    }

    private GameStateResponse toResponse(RaceGame game, TurnResult lastTurn) {
        return new GameStateResponse(
                game.getId(),
                game.getDistance(),
                game.getTurn(),
                game.getHorses(),
                game.getPositions(),
                game.getWinner(),
                lastTurn,
                game.getHistory()
        );
    }
}
