
package com.cituccs.sims.service;

import com.cituccs.sims.entity.CancellationPolicy;
import com.cituccs.sims.repository.CancellationPolicyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CancellationPolicyService {

    private final CancellationPolicyRepository cancellationPolicyRepository;

    public CancellationPolicyService(CancellationPolicyRepository cancellationPolicyRepository) {
        this.cancellationPolicyRepository = cancellationPolicyRepository;
    }

    public List<CancellationPolicy> getAllPolicies() {
        return cancellationPolicyRepository.findAll();
    }

    public CancellationPolicy getPolicyById(Long id) {
        return cancellationPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cancellation policy not found"));
    }

    public CancellationPolicy savePolicy(CancellationPolicy policy) {
        return cancellationPolicyRepository.save(policy);
    }

    public void deletePolicy(Long id) {
        cancellationPolicyRepository.deleteById(id);
    }
}
