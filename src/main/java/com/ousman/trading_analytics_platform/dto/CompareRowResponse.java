package com.ousman.trading_analytics_platform.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

// One row per trading day, with each fund's % change from the start of the window.
// I flatten the map into the row with @JsonAnyGetter so the JSON stays
// {"date": ..., "SPY": 1.23, "QQQ": 2.34}, which is the shape Recharts plots directly.
public record CompareRowResponse(
        LocalDate date,
        @JsonAnyGetter Map<String, BigDecimal> changePctByTicker
) {
}
