package com.example.carreracaballos.api;

public record GroupSummaryResponse(
        Long id,
        String code,
        long memberCount,
        long maxMembers,
        long maxGroups
) {
}
