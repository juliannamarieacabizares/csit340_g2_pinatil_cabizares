
package com.cituccs.sims.service;

import com.cituccs.sims.entity.TimeSlot;
import com.cituccs.sims.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    public List<TimeSlot> getAllTimeSlots() {
        return timeSlotRepository.findAll();
    }

    public TimeSlot getTimeSlotById(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));
    }

    public TimeSlot saveTimeSlot(TimeSlot timeSlot) {
        return timeSlotRepository.save(timeSlot);
    }

    public void deleteTimeSlot(Long id) {
        timeSlotRepository.deleteById(id);
    }
}
