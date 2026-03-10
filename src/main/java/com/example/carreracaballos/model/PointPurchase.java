package com.example.carreracaballos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "point_purchases")
public class PointPurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false)
    private long pointsPurchased;

    @Column(nullable = false)
    private long amountCop;

    @Column(nullable = false)
    private Instant createdAt;

    protected PointPurchase() {
    }

    public PointPurchase(UserAccount user, long pointsPurchased, long amountCop, Instant createdAt) {
        this.user = user;
        this.pointsPurchased = pointsPurchased;
        this.amountCop = amountCop;
        this.createdAt = createdAt;
    }
}
