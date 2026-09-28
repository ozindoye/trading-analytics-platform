package com.ousman.trading_analytics_platform.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MetricsResponse(
        String ticker,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal startClose,
        BigDecimal endClose,
        BigDecimal periodReturnPct,
        BigDecimal annualisedVolatilityPct,
        BigDecimal maxDrawdownPct,
        BigDecimal sharpeRatio,
        BigDecimal high,
        BigDecimal low
) {
}
