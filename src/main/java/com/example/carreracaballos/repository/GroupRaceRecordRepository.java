package com.example.carreracaballos.repository;

import com.example.carreracaballos.model.GameStatus;
import com.example.carreracaballos.model.GroupRaceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GroupRaceRecordRepository extends JpaRepository<GroupRaceRecord, UUID> {
    Optional<GroupRaceRecord> findByGroupIdAndStatus(Long groupId, GameStatus status);
}
