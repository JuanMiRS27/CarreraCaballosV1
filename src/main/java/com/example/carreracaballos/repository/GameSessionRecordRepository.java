package com.example.carreracaballos.repository;

import com.example.carreracaballos.model.GameSessionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GameSessionRecordRepository extends JpaRepository<GameSessionRecord, UUID> {
    List<GameSessionRecord> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    List<GameSessionRecord> findByRaceIdOrderByCreatedAtAsc(UUID raceId);

    boolean existsByRaceIdAndUserId(UUID raceId, Long userId);
}
