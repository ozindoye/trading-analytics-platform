package com.ousman.trading_analytics_platform.repository;

import com.ousman.trading_analytics_platform.model.Fund;
import com.ousman.trading_analytics_platform.model.PriceBar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface PriceBarRepository extends JpaRepository<PriceBar, Long> {

    List<PriceBar> findByFundOrderByDateAsc(Fund fund);

    // Only the date column, not whole entities: ingestion just needs to know which days
    // are already stored, and one query here replaces one query per incoming bar.
    @Query("select p.date from PriceBar p where p.fund = :fund")
    Set<LocalDate> findDatesByFund(@Param("fund") Fund fund);

    List<PriceBar> findByFundAndDateGreaterThanEqualOrderByDateAsc(Fund fund, LocalDate from);

    List<PriceBar> findByFundAndDateBetweenOrderByDateAsc(Fund fund, LocalDate from, LocalDate to);
}
