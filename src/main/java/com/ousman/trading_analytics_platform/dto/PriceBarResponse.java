package com.ousman.trading_analytics_platform.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PriceBarResponse(LocalDate date, BigDecimal close) {
}