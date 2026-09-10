package com.ousman.trading_analytics_platform.client;

import java.util.List;

public record TwelveDataTimeSeriesResponse(
        String status,
        List<TwelveDataValue> values
) {
}
