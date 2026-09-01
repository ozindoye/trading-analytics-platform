package com.ousman.trading_analytics_platform.repository;

import com.ousman.trading_analytics_platform.model.Fund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FundRepository extends JpaRepository<Fund, Long> {

    Optional<Fund> findByTicker(String ticker);
}