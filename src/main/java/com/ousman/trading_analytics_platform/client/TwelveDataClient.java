package com.ousman.trading_analytics_platform.client;

import com.ousman.trading_analytics_platform.config.TwelveDataProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TwelveDataClient {

    private final RestClient restClient;
    private final TwelveDataProperties properties;

    public TwelveDataClient(TwelveDataProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create(properties.baseUrl());
    }

    public TwelveDataTimeSeriesResponse fetchDailySeries(String symbol, int outputSize) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/time_series")
                        .queryParam("symbol", symbol)
                        .queryParam("interval", "1day")
                        .queryParam("outputsize", outputSize)
                        .queryParam("apikey", properties.apiKey())
                        .build())
                .retrieve()
                .body(TwelveDataTimeSeriesResponse.class);
    }
}