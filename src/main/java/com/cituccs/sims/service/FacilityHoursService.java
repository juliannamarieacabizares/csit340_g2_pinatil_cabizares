
package com.cituccs.sims.service;

import com.cituccs.sims.entity.FacilityHours;
import com.cituccs.sims.repository.FacilityHoursRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FacilityHoursService {

    private final FacilityHoursRepository facilityHoursRepository;

    public FacilityHoursService(FacilityHoursRepository facilityHoursRepository) {
        this.facilityHoursRepository = facilityHoursRepository;
    }

    public List<FacilityHours> getAllFacilityHours() {
        return facilityHoursRepository.findAll();
    }

    public FacilityHours getFacilityHoursById(Long id) {
        return facilityHoursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facility hours not found"));
    }

    public FacilityHours saveFacilityHours(FacilityHours hours) {
        return facilityHoursRepository.save(hours);
    }

    public void deleteFacilityHours(Long id) {
        facilityHoursRepository.deleteById(id);
    }
}
