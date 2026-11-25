
package com.cituccs.sims.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "facility_hours")
public class FacilityHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facility_hours_id")
    private Long facilityHoursId;

    @Column(name = "day_of_week", nullable = false, length = 15)
    private String dayOfWeek;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "is_open", nullable = false)
    private boolean open = true;

    public FacilityHours() {
    }

    public Long getFacilityHoursId() {
        return facilityHoursId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
