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
@Table(name = "game_sessions")
public class GameSessionRecord {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupRoom group;

    @Column(nullable = false)
    private int distance;

    @Column(nullable = false, length = 64)
    private String horsesCsv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SuitSpanish selectedHorse;

    @Column(nullable = false)
    private long betPoints;

    @Column(nullable = false)
    private long payoutPoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private SuitSpanish winnerHorse;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant finishedAt;

    protected GameSessionRecord() {
    }

    public GameSessionRecord(UUID id, UserAccount user, GroupRoom group, int distance, String horsesCsv,
                             SuitSpanish selectedHorse, long betPoints, long payoutPoints, GameStatus status,
                             Instant createdAt) {
        this.id = id;
        this.user = user;
        this.group = group;
        this.distance = distance;
        this.horsesCsv = horsesCsv;
        this.selectedHorse = selectedHorse;
        this.betPoints = betPoints;
        this.payoutPoints = payoutPoints;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public GroupRoom getGroup() {
        return group;
    }

    public SuitSpanish getSelectedHorse() {
        return selectedHorse;
    }

    public long getBetPoints() {
        return betPoints;
    }

    public long getPayoutPoints() {
        return payoutPoints;
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

    public void finish(SuitSpanish winnerHorse, long payoutPoints, Instant finishedAt) {
        this.winnerHorse = winnerHorse;
        this.payoutPoints = payoutPoints;
        this.finishedAt = finishedAt;
        this.status = GameStatus.FINISHED;
    }
}
