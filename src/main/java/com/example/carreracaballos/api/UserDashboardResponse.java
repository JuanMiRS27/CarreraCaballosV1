package com.example.carreracaballos.api;

import java.util.List;

public record UserDashboardResponse(
        Long userId,
        String name,
        String email,
        long pointsBalance,
        GroupSummaryResponse group,
        ActiveGroupGameResponse activeGame,
        List<RecentGameResponse> recentGames
) {
}
