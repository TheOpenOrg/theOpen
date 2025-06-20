package org.theopen.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.theopen.backend.model.Config;

import java.util.List;
import java.util.UUID;

public interface ConfigRepository extends JpaRepository<Config, UUID> {
    List<Config> findAllByUser_TgId(Long tgId);
    boolean existsByUser_TgIdAndMonthAmount(Long tgId, int monthAmount);
    List<Config> findAllByPaymentPaymentId(Long paymentId);
}

