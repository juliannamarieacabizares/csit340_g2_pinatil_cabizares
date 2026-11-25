
package com.cituccs.sims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "courts")
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "court_id")
    private Long courtId;

    @Column(name = "court_name", nullable = false, length = 100)
    private String courtName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "hourly_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "court_status", nullable = false, length = 20)
    private String courtStatus = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Court() {
    }

    public Long getCourtId() {
        return courtId;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getCourtStatus() {
        return courtStatus;
    }

    public void setCourtStatus(String courtStatus) {
        this.courtStatus = courtStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
