package org.theopen.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.theopen.backend.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByPaymentId(Long paymentId);
}

