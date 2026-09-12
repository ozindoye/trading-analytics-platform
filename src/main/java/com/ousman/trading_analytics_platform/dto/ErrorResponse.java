package com.ousman.trading_analytics_platform.dto;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, String message) {
}
