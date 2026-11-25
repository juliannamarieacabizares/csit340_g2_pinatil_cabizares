
package com.cituccs.sims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancellation_policies")
public class CancellationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "policy_name", nullable = false, length = 100)
    private String policyName;

    @Column(name = "hours_before_booking", nullable = false)
    private Integer hoursBeforeBooking;

    @Column(name = "refund_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal refundPercentage;

    @Column(name = "policy_description", columnDefinition = "TEXT")
    private String policyDescription;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public CancellationPolicy() {
    }

    public Long getPolicyId() {
        return policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Integer getHoursBeforeBooking() {
        return hoursBeforeBooking;
    }

    public void setHoursBeforeBooking(Integer hoursBeforeBooking) {
        this.hoursBeforeBooking = hoursBeforeBooking;
    }

    public BigDecimal getRefundPercentage() {
        return refundPercentage;
    }

    public void setRefundPercentage(BigDecimal refundPercentage) {
        this.refundPercentage = refundPercentage;
    }

    public String getPolicyDescription() {
        return policyDescription;
    }

    public void setPolicyDescription(String policyDescription) {
        this.policyDescription = policyDescription;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

