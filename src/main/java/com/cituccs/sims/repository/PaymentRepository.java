package com.cituccs.sims.repository;

import com.cituccs.sims.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Newest payment first
    List<Payment> findByBookingBookingIdOrderByPaymentIdDesc(Long bookingId);

    // Stops the same GCash reference from being used for two bookings
    // (a rejected payment does not count).
    boolean existsByTransactionReferenceAndPaymentStatusNot(String transactionReference, String paymentStatus);
}
