package com.example.carreracaballos.service;

import com.example.carreracaballos.api.ActiveGroupGameResponse;
import com.example.carreracaballos.api.GameParticipantResponse;
import com.example.carreracaballos.api.GameStateResponse;
import com.example.carreracaballos.game.RaceGame;
import com.example.carreracaballos.game.SuitSpanish;
import com.example.carreracaballos.game.TurnResult;
import com.example.carreracaballos.model.GameSessionRecord;
import com.example.carreracaballos.model.GameStatus;
import com.example.carreracaballos.model.GroupRaceRecord;
import com.example.carreracaballos.model.GroupRoom;
import com.example.carreracaballos.model.UserAccount;
import com.example.carreracaballos.repository.GameSessionRecordRepository;
import com.example.carreracaballos.repository.GroupRaceRecordRepository;
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
    private final ConcurrentHashMap<UUID, ActiveRace> games = new ConcurrentHashMap<>();
    private final UserAccountService userAccountService;
    private final GroupRoomService groupRoomService;
    private final GameSessionRecordRepository gameSessionRecordRepository;
    private final GroupRaceRecordRepository groupRaceRecordRepository;

    public RaceGameService(UserAccountService userAccountService, GroupRoomService groupRoomService,
                           GameSessionRecordRepository gameSessionRecordRepository,
                           GroupRaceRecordRepository groupRaceRecordRepository) {
        this.userAccountService = userAccountService;
        this.groupRoomService = groupRoomService;
        this.gameSessionRecordRepository = gameSessionRecordRepository;
        this.groupRaceRecordRepository = groupRaceRecordRepository;
    }

    @Transactional
    public GameStateResponse createGame(Long userId, int distance, List<SuitSpanish> selectedHorses,
                                        SuitSpanish selectedHorse, long betPoints) {
        ValidationResult validation = validateRequest(distance, selectedHorses, selectedHorse);
        UserAccount user = userAccountService.getRequiredUser(userId);
        GroupRoom group = groupRoomService.getRequiredGroupForUser(userId);
        ensureBalance(user, betPoints);

        GroupRaceRecord raceRecord = groupRaceRecordRepository.findByGroupIdAndStatus(group.getId(), GameStatus.ACTIVE)
                .orElseGet(() -> createSharedRace(group, validation.distance(), validation.horses()));

        ActiveRace activeRace = getRequiredRace(raceRecord.getId());
        if (activeRace.record().getGroup().getId().longValue() != group.getId().longValue()) {
            throw new IllegalArgumentException("La carrera activa no pertenece al grupo del usuario.");
        }
        if (activeRace.record().getStatus() != GameStatus.ACTIVE) {
            throw new IllegalArgumentException("La carrera seleccionada ya finalizo.");
        }
        if (activeRace.game().getDistance() != distance || !activeRace.game().getHorses().equals(validation.horses())) {
            throw new IllegalArgumentException("Ya existe una carrera activa para tu grupo. Debes entrar a esa misma carrera.");
        }
        if (gameSessionRecordRepository.existsByRaceIdAndUserId(activeRace.record().getId(), userId)) {
            throw new IllegalArgumentException("Este usuario ya tiene una apuesta registrada en la carrera activa.");
        }

        GameSessionRecord betRecord = createBetRecord(user, group, activeRace.record(), selectedHorse, betPoints, validation);
        activeRace.participants().add(betRecord);
        return toResponse(activeRace, userId, null);
    }

    @Transactional(readOnly = true)
    public GameStateResponse getGame(UUID id, Long userId) {
        ActiveRace activeRace = getRequiredRace(id);
        return toResponse(activeRace, userId, null);
    }

    @Transactional(readOnly = true)
    public ActiveGroupGameResponse getActiveGameForUser(Long userId) {
        GroupRoom group = groupRoomService.getRequiredGroupForUser(userId);
        GroupRaceRecord raceRecord = groupRaceRecordRepository.findByGroupIdAndStatus(group.getId(), GameStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No hay una carrera activa para este grupo."));
        ActiveRace activeRace = getRequiredRace(raceRecord.getId());
        return new ActiveGroupGameResponse(
                activeRace.record().getId(),
                activeRace.game().getDistance(),
                activeRace.game().getTurn(),
                activeRace.game().getHorses(),
                activeRace.record().getStatus(),
                activeRace.participants().size()
        );
    }

    @Transactional
    public GameStateResponse step(UUID id, Long userId) {
        ActiveRace activeRace = getRequiredRace(id);
        TurnResult lastTurn = activeRace.game().step();
        if (activeRace.game().getWinner() != null && activeRace.record().getStatus() == GameStatus.ACTIVE) {
            settleGame(activeRace);
        }
        return toResponse(activeRace, userId, lastTurn);
    }

    private ValidationResult validateRequest(int distance, List<SuitSpanish> selectedHorses, SuitSpanish selectedHorse) {
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
        return new ValidationResult(distance, new ArrayList<>(deduplicated));
    }

    private void ensureBalance(UserAccount user, long betPoints) {
        if (betPoints < 1) {
            throw new IllegalArgumentException("La apuesta debe ser mayor a cero.");
        }
        if (user.getPointsBalance() < betPoints) {
            throw new IllegalArgumentException("No tienes puntos suficientes para realizar esa apuesta.");
        }
    }

    private GroupRaceRecord createSharedRace(GroupRoom group, int distance, List<SuitSpanish> horses) {
        UUID raceId = UUID.randomUUID();
        GroupRaceRecord raceRecord = new GroupRaceRecord(
                raceId,
                group,
                distance,
                horses.stream().map(Enum::name).reduce((left, right) -> left + "," + right).orElse(""),
                GameStatus.ACTIVE,
                Instant.now()
        );
        groupRaceRecordRepository.save(raceRecord);
        games.put(raceId, new ActiveRace(new RaceGame(raceId, distance, horses), raceRecord, new ArrayList<>()));
        return raceRecord;
    }

    private GameSessionRecord createBetRecord(UserAccount user, GroupRoom group, GroupRaceRecord raceRecord,
                                              SuitSpanish selectedHorse, long betPoints, ValidationResult validation) {
        user.setPointsBalance(user.getPointsBalance() - betPoints);
        userAccountService.save(user);
        GameSessionRecord betRecord = new GameSessionRecord(
                UUID.randomUUID(),
                user,
                group,
                raceRecord,
                validation.distance(),
                validation.horses().stream().map(Enum::name).reduce((left, right) -> left + "," + right).orElse(""),
                selectedHorse,
                betPoints,
                0,
                GameStatus.ACTIVE,
                Instant.now()
        );
        return gameSessionRecordRepository.save(betRecord);
    }

    private ActiveRace getRequiredRace(UUID id) {
        ActiveRace activeRace = games.get(id);
        if (activeRace != null) {
            return activeRace;
        }

        GroupRaceRecord raceRecord = groupRaceRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La carrera no existe."));
        if (raceRecord.getStatus() != GameStatus.ACTIVE) {
            throw new IllegalArgumentException("La carrera no existe o ya finalizo.");
        }
        throw new IllegalArgumentException("La carrera activa no puede recuperarse despues de reiniciar el servidor.");
    }

    private void settleGame(ActiveRace activeRace) {
        SuitSpanish winner = activeRace.game().getWinner();
        activeRace.record().finish(winner, Instant.now());
        groupRaceRecordRepository.save(activeRace.record());

        for (GameSessionRecord participant : activeRace.participants()) {
            long payout = participant.getSelectedHorse() == winner ? participant.getBetPoints() * 5 : 0;
            if (payout > 0) {
                UserAccount bettor = participant.getUser();
                bettor.setPointsBalance(bettor.getPointsBalance() + payout);
                userAccountService.save(bettor);
            }
            participant.finish(winner, payout, Instant.now());
        }
        gameSessionRecordRepository.saveAll(activeRace.participants());
    }

    private GameStateResponse toResponse(ActiveRace activeRace, Long currentUserId, TurnResult lastTurn) {
        GameSessionRecord currentBet = activeRace.participants().stream()
                .filter(item -> item.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElse(null);
        UserAccount currentUser = currentUserId == null ? null : userAccountService.getRequiredUser(currentUserId);

        return new GameStateResponse(
                activeRace.record().getId(),
                currentUserId,
                currentUser != null ? currentUser.getName() : null,
                activeRace.record().getGroup().getCode(),
                activeRace.game().getDistance(),
                activeRace.game().getTurn(),
                activeRace.game().getHorses(),
                currentBet != null ? currentBet.getSelectedHorse() : null,
                currentBet != null ? currentBet.getBetPoints() : 0,
                currentBet != null ? currentBet.getPayoutPoints() : 0,
                currentUser != null ? currentUser.getPointsBalance() : 0,
                activeRace.game().getPositions(),
                activeRace.game().getWinner(),
                activeRace.record().getStatus(),
                activeRace.participants().stream().map(item -> new GameParticipantResponse(
                        item.getUser().getId(),
                        item.getUser().getName(),
                        item.getSelectedHorse(),
                        item.getBetPoints(),
                        item.getPayoutPoints(),
                        item.getUser().getId().equals(currentUserId)
                )).toList(),
                lastTurn,
                activeRace.game().getHistory()
        );
    }

    private record ActiveRace(RaceGame game, GroupRaceRecord record, List<GameSessionRecord> participants) {
    }

    private record ValidationResult(int distance, List<SuitSpanish> horses) {
    }
}
