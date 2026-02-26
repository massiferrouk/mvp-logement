package com.massi.mvplogement.exchange;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_requests")
public class ExchangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // période de l'émetteur (moi)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_period_id", nullable = false)
    private ExchangePeriod fromPeriod;

    // période du destinataire (l'autre)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_period_id", nullable = false)
    private ExchangePeriod toPeriod;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = "PENDING";
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public ExchangePeriod getFromPeriod() { return fromPeriod; }
    public void setFromPeriod(ExchangePeriod fromPeriod) { this.fromPeriod = fromPeriod; }

    public ExchangePeriod getToPeriod() { return toPeriod; }
    public void setToPeriod(ExchangePeriod toPeriod) { this.toPeriod = toPeriod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}