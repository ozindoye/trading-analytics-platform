package com.ousman.trading_analytics_platform.repository;

import com.ousman.trading_analytics_platform.model.Fund;
import com.ousman.trading_analytics_platform.model.PriceBar;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Runs the real JPQL against H2, because a typo in a @Query only shows up when it executes.
@DataJpaTest
class PriceBarRepositoryTest {

    @Autowired
    private FundRepository fundRepository;

    @Autowired
    private PriceBarRepository priceBarRepository;

    private PriceBar bar(Fund fund, String date) {
        BigDecimal price = new BigDecimal("100");
        return new PriceBar(fund, LocalDate.parse(date), price, price, price, price, 1_000L);
    }

    @Test
    void findDatesByFundReturnsOnlyThatFundsDates() {
        Fund spy = fundRepository.save(new Fund("SPY", "Test Fund A"));
        Fund qqq = fundRepository.save(new Fund("QQQ", "Test Fund B"));
        priceBarRepository.saveAll(List.of(
                bar(spy, "2025-01-02"),
                bar(spy, "2025-01-03"),
                bar(qqq, "2025-01-06")
        ));

        Set<LocalDate> dates = priceBarRepository.findDatesByFund(spy);

        assertEquals(Set.of(LocalDate.parse("2025-01-02"), LocalDate.parse("2025-01-03")), dates);
    }
}
