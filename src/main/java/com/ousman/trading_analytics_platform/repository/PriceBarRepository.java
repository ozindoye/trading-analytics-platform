package com.ousman.trading_analytics_platform.repository;

import com.ousman.trading_analytics_platform.model.Fund;
import com.ousman.trading_analytics_platform.model.PriceBar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PriceBarRepository extends JpaRepository<PriceBar, Long> {

    List<PriceBar> findByFundOrderByDateAsc(Fund fund);

    boolean existsByFundAndDate(Fund fund, LocalDate date);

    List<PriceBar> findByFundAndDateGreaterThanEqualOrderByDateAsc(Fund fund, LocalDate from);
}