
package com.cituccs.sims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_cancellations")
public class BookingCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancellation_id")
    private Long cancellationId;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "cancellation_status", nullable = false, length = 20)
    private String cancellationStatus = "PENDING";

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
    }

    public BookingCancellation() {
    }

    public Long getCancellationId() {
        return cancellationId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCancellationStatus() {
        return cancellationStatus;
    }

    public void setCancellationStatus(String cancellationStatus) {
        this.cancellationStatus = cancellationStatus;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public User getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(User processedBy) {
        this.processedBy = processedBy;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
