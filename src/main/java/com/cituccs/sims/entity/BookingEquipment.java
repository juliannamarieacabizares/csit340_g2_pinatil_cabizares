
package com.cituccs.sims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_equipment")
public class BookingEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_equipment_id")
    private Long bookingEquipmentId;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitFee;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    public BookingEquipment() {
    }

    public Long getBookingEquipmentId() {
        return bookingEquipmentId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitFee() {
        return unitFee;
    }

    public void setUnitFee(BigDecimal unitFee) {
        this.unitFee = unitFee;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
