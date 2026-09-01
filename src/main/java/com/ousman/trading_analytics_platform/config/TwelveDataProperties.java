package com.ousman.trading_analytics_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "twelvedata")
public record TwelveDataProperties(String baseUrl, String apiKey) {
}