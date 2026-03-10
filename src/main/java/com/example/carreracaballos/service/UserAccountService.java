package com.example.carreracaballos.service;

import com.example.carreracaballos.api.AuthResponse;
import com.example.carreracaballos.api.GroupSummaryResponse;
import com.example.carreracaballos.api.RecentGameResponse;
import com.example.carreracaballos.api.UserDashboardResponse;
import com.example.carreracaballos.model.GameSessionRecord;
import com.example.carreracaballos.model.UserAccount;
import com.example.carreracaballos.repository.GameSessionRecordRepository;
import com.example.carreracaballos.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class UserAccountService {
    public static final long INITIAL_POINTS = 1_000L;

    private final UserAccountRepository userAccountRepository;
    private final PasswordHasher passwordHasher;
    private final GroupRoomService groupRoomService;
    private final GameSessionRecordRepository gameSessionRecordRepository;

    public UserAccountService(UserAccountRepository userAccountRepository, PasswordHasher passwordHasher,
                              GroupRoomService groupRoomService, GameSessionRecordRepository gameSessionRecordRepository) {
        this.userAccountRepository = userAccountRepository;
        this.passwordHasher = passwordHasher;
        this.groupRoomService = groupRoomService;
        this.gameSessionRecordRepository = gameSessionRecordRepository;
    }

    @Transactional
    public AuthResponse register(String name, String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo.");
        }

        UserAccount user = userAccountRepository.save(new UserAccount(
                name.trim(),
                normalizedEmail,
                passwordHasher.hash(password),
                INITIAL_POINTS,
                Instant.now()
        ));
        GroupSummaryResponse group = groupRoomService.assignUser(user);
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getPointsBalance(), group);
    }

    @Transactional
    public AuthResponse login(String email, String password) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales invalidas."));
        if (!user.getPasswordHash().equals(passwordHasher.hash(password))) {
            throw new IllegalArgumentException("Credenciales invalidas.");
        }
        GroupSummaryResponse group = groupRoomService.assignUser(user);
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getPointsBalance(), group);
    }

    @Transactional(readOnly = true)
    public UserDashboardResponse getDashboard(Long userId) {
        UserAccount user = getRequiredUser(userId);
        GroupSummaryResponse group = groupRoomService.getSummaryForUser(userId);
        List<RecentGameResponse> recentGames = gameSessionRecordRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toRecentGame)
                .toList();
        return new UserDashboardResponse(user.getId(), user.getName(), user.getEmail(), user.getPointsBalance(), group, recentGames);
    }

    @Transactional(readOnly = true)
    public UserAccount getRequiredUser(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
    }

    @Transactional
    public UserAccount save(UserAccount user) {
        return userAccountRepository.save(user);
    }

    private RecentGameResponse toRecentGame(GameSessionRecord game) {
        return new RecentGameResponse(
                game.getId(),
                game.getSelectedHorse(),
                game.getWinnerHorse(),
                game.getBetPoints(),
                game.getPayoutPoints(),
                game.getStatus(),
                game.getCreatedAt()
        );
    }
}
