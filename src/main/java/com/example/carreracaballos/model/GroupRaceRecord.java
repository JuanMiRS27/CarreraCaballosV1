package com.example.carreracaballos.model;

import com.example.carreracaballos.game.SuitSpanish;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "group_races")
public class GroupRaceRecord {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupRoom group;

    @Column(nullable = false)
    private int distance;

    @Column(nullable = false, length = 64)
    private String horsesCsv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private SuitSpanish winnerHorse;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant finishedAt;

    protected GroupRaceRecord() {
    }

    public GroupRaceRecord(UUID id, GroupRoom group, int distance, String horsesCsv, GameStatus status, Instant createdAt) {
        this.id = id;
        this.group = group;
        this.distance = distance;
        this.horsesCsv = horsesCsv;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public GroupRoom getGroup() {
        return group;
    }

    public int getDistance() {
        return distance;
    }

    public String getHorsesCsv() {
        return horsesCsv;
    }

    public GameStatus getStatus() {
        return status;
    }

    public SuitSpanish getWinnerHorse() {
        return winnerHorse;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void finish(SuitSpanish winnerHorse, Instant finishedAt) {
        this.winnerHorse = winnerHorse;
        this.finishedAt = finishedAt;
        this.status = GameStatus.FINISHED;
    }
}
