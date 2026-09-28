package com.ousman.trading_analytics_platform.service;

import com.ousman.trading_analytics_platform.dto.MetricsResponse;
import com.ousman.trading_analytics_platform.model.Fund;
import com.ousman.trading_analytics_platform.model.PriceBar;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetricsCalculatorTest {

    private final Fund fund = new Fund("SPY", "Test Fund");

    private PriceBar bar(String date, String high, String low, String close) {
        return new PriceBar(fund, LocalDate.parse(date),
                new BigDecimal(close), new BigDecimal(high), new BigDecimal(low),
                new BigDecimal(close), 1_000_000L);
    }

    @Test
    void computesReturnHighLowAndZeroVolatilityForConstantGrowth() {
        List<PriceBar> bars = List.of(
                bar("2025-01-01", "101", "99", "100"),
                bar("2025-01-02", "112", "108", "110"),
                bar("2025-01-03", "122", "119", "121")
        );

        MetricsResponse m = MetricsCalculator.computeMetrics("SPY", bars);

        assertEquals(new BigDecimal("21.00"), m.periodReturnPct());
        assertEquals(new BigDecimal("0.00"), m.annualisedVolatilityPct());
        assertEquals(0, m.high().compareTo(new BigDecimal("122")));
        assertEquals(0, m.low().compareTo(new BigDecimal("99")));
        assertEquals(LocalDate.parse("2025-01-03"), m.endDate());
        assertEquals(new BigDecimal("0.00"), m.maxDrawdownPct());
        assertEquals(new BigDecimal("0.00"), m.sharpeRatio());
    }

    @Test
    void computesNonZeroAnnualisedVolatility() {
        List<PriceBar> bars = List.of(
                bar("2025-01-01", "100", "100", "100"),
                bar("2025-01-02", "110", "110", "110"),
                bar("2025-01-03", "99", "99", "99")
        );

        MetricsResponse m = MetricsCalculator.computeMetrics("SPY", bars);

        assertEquals(new BigDecimal("-1.00"), m.periodReturnPct());
        assertEquals(224.50, m.annualisedVolatilityPct().doubleValue(), 0.1);
        assertEquals(new BigDecimal("10.00"), m.maxDrawdownPct());
        assertEquals(new BigDecimal("0.00"), m.sharpeRatio());
    }

    @Test
    void computesMaximumDrawdown() {
        List<PriceBar> bars = List.of(
                bar("2025-01-01", "100", "100", "100"),
                bar("2025-01-02", "120", "120", "120"),
                bar("2025-01-03", "90", "90", "90"),
                bar("2025-01-04", "110", "110", "110")
        );
        MetricsResponse m = MetricsCalculator.computeMetrics("SPY", bars);
        assertEquals(new BigDecimal("25.00"), m.maxDrawdownPct()); // fell from 120 to 90
    }

    @Test
    void throwsWhenTooFewBars() {
        List<PriceBar> bars = List.of(bar("2025-01-01", "100", "100", "100"));
        assertThrows(IllegalArgumentException.class,
                () -> MetricsCalculator.computeMetrics("SPY", bars));
    }
}
