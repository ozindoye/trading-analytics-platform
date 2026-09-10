package com.ousman.trading_analytics_platform.client;

public record TwelveDataValue(
        String datetime,
        String open,
        String high,
        String low,
        String close,
        String volume
) {
}
