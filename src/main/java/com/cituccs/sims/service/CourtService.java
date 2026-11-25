
package com.cituccs.sims.service;

import com.cituccs.sims.entity.Court;
import com.cituccs.sims.repository.CourtRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CourtService {

    private final CourtRepository courtRepository;

    public CourtService(CourtRepository courtRepository) {
        this.courtRepository = courtRepository;
    }

    public List<Court> getAllCourts() {
        return courtRepository.findAll();
    }

    public Court getCourtById(Long id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Court not found"));
    }

    public Court saveCourt(Court court) {
        return courtRepository.save(court);
    }

    public void deleteCourt(Long id) {
        courtRepository.deleteById(id);
    }
}
