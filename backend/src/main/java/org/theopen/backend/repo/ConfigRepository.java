package org.theopen.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.theopen.backend.model.Config;

import java.util.List;

public interface ConfigRepository extends JpaRepository<Config, Long> {
    List<Config> findAllByUser_Id(Long userId);
    boolean existsByUser_IdAndMonthAmount(Long uid, int monthAmount);
}