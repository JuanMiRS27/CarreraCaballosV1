package com.example.carreracaballos.service;

import com.example.carreracaballos.api.GameStateResponse;
import com.example.carreracaballos.game.RaceGame;
import com.example.carreracaballos.game.SuitSpanish;
import com.example.carreracaballos.game.TurnResult;
import com.example.carreracaballos.model.GameSessionRecord;
import com.example.carreracaballos.model.GameStatus;
import com.example.carreracaballos.model.GroupRoom;
import com.example.carreracaballos.model.UserAccount;
import com.example.carreracaballos.repository.GameSessionRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RaceGameService {
    private final ConcurrentHashMap<UUID, ActiveGame> games = new ConcurrentHashMap<>();
    private final UserAccountService userAccountService;
    private final GroupRoomService groupRoomService;
    private final GameSessionRecordRepository gameSessionRecordRepository;

    public RaceGameService(UserAccountService userAccountService, GroupRoomService groupRoomService,
                           GameSessionRecordRepository gameSessionRecordRepository) {
        this.userAccountService = userAccountService;
        this.groupRoomService = groupRoomService;
        this.gameSessionRecordRepository = gameSessionRecordRepository;
    }

    @Transactional
    public GameStateResponse createGame(Long userId, int distance, List<SuitSpanish> selectedHorses,
                                        SuitSpanish selectedHorse, long betPoints) {
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
        if (!deduplicated.contains(selectedHorse)) {
            throw new IllegalArgumentException("El caballo apostado debe estar dentro de los caballos seleccionados.");
        }

        UserAccount user = userAccountService.getRequiredUser(userId);
        GroupRoom group = groupRoomService.getRequiredGroupForUser(userId);
        if (user.getPointsBalance() < betPoints) {
            throw new IllegalArgumentException("No tienes puntos suficientes para realizar esa apuesta.");
        }

        UUID id = UUID.randomUUID();
        RaceGame game = new RaceGame(id, distance, new ArrayList<>(deduplicated));
        user.setPointsBalance(user.getPointsBalance() - betPoints);
        userAccountService.save(user);

        GameSessionRecord record = new GameSessionRecord(
                id,
                user,
                group,
                distance,
                deduplicated.stream().map(Enum::name).reduce((left, right) -> left + "," + right).orElse(""),
                selectedHorse,
                betPoints,
                0,
                GameStatus.ACTIVE,
                Instant.now()
        );
        gameSessionRecordRepository.save(record);

        ActiveGame activeGame = new ActiveGame(game, record, user);
        games.put(id, activeGame);
        return toResponse(activeGame, null);
    }

    @Transactional(readOnly = true)
    public GameStateResponse getGame(UUID id) {
        ActiveGame activeGame = getRequiredGame(id);
        return toResponse(activeGame, null);
    }

    @Transactional
    public GameStateResponse step(UUID id) {
        ActiveGame activeGame = getRequiredGame(id);
        TurnResult lastTurn = activeGame.game().step();
        if (activeGame.game().getWinner() != null && activeGame.record().getStatus() == GameStatus.ACTIVE) {
            settleGame(activeGame);
        }
        return toResponse(activeGame, lastTurn);
    }

    private ActiveGame getRequiredGame(UUID id) {
        ActiveGame activeGame = games.get(id);
        if (activeGame == null) {
            throw new IllegalArgumentException("La carrera no existe o se perdio tras reiniciar el servidor.");
        }
        return activeGame;
    }

    private void settleGame(ActiveGame activeGame) {
        SuitSpanish winner = activeGame.game().getWinner();
        long payout = winner == activeGame.record().getSelectedHorse()
                ? activeGame.record().getBetPoints() * 5
                : 0;
        if (payout > 0) {
            UserAccount user = activeGame.user();
            user.setPointsBalance(user.getPointsBalance() + payout);
            userAccountService.save(user);
        }
        activeGame.record().finish(winner, payout, Instant.now());
        gameSessionRecordRepository.save(activeGame.record());
    }

    private GameStateResponse toResponse(ActiveGame activeGame, TurnResult lastTurn) {
        return new GameStateResponse(
                activeGame.game().getId(),
                activeGame.user().getId(),
                activeGame.user().getName(),
                activeGame.record().getGroup().getCode(),
                activeGame.game().getDistance(),
                activeGame.game().getTurn(),
                activeGame.game().getHorses(),
                activeGame.record().getSelectedHorse(),
                activeGame.record().getBetPoints(),
                activeGame.record().getPayoutPoints(),
                activeGame.user().getPointsBalance(),
                activeGame.game().getPositions(),
                activeGame.game().getWinner(),
                activeGame.record().getStatus(),
                lastTurn,
                activeGame.game().getHistory()
        );
    }

    private record ActiveGame(RaceGame game, GameSessionRecord record, UserAccount user) {
    }
}
