
package com.cituccs.sims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "booking_reference", nullable = false, unique = true, length = 30)
    private String bookingReference;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private TimeSlot timeSlot;

    @ManyToOne
    @JoinColumn(name = "policy_id")
    private CancellationPolicy cancellationPolicy;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "booking_status", nullable = false, length = 20)
    private String bookingStatus = "PENDING";

    @Column(name = "court_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal courtAmount = BigDecimal.ZERO;

    @Column(name = "equipment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal equipmentAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "cancellation_deadline")
    private LocalDateTime cancellationDeadline;

    @Column(name = "refund_percentage", precision = 5, scale = 2)
    private BigDecimal refundPercentage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Booking() {
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Court getCourt() {
        return court;
    }

    public void setCourt(Court court) {
        this.court = court;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public CancellationPolicy getCancellationPolicy() {
    return cancellationPolicy;
    }

    public void setCancellationPolicy(CancellationPolicy cancellationPolicy) {
    this.cancellationPolicy = cancellationPolicy;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public BigDecimal getCourtAmount() {
        return courtAmount;
    }

    public void setCourtAmount(BigDecimal courtAmount) {
        this.courtAmount = courtAmount;
    }

    public BigDecimal getEquipmentAmount() {
        return equipmentAmount;
    }

    public void setEquipmentAmount(BigDecimal equipmentAmount) {
        this.equipmentAmount = equipmentAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCancellationDeadline() {
        return cancellationDeadline;
    }

    public void setCancellationDeadline(LocalDateTime cancellationDeadline) {
        this.cancellationDeadline = cancellationDeadline;
    }

    public BigDecimal getRefundPercentage() {
        return refundPercentage;
    }

    public void setRefundPercentage(BigDecimal refundPercentage) {
        this.refundPercentage = refundPercentage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
