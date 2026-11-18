package com.ousman.trading_analytics_platform.controller;

import com.ousman.trading_analytics_platform.dto.MetricsResponse;
import com.ousman.trading_analytics_platform.service.MetricsService;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static com.ousman.trading_analytics_platform.controller.PriceController.TICKER_MESSAGE;
import static com.ousman.trading_analytics_platform.controller.PriceController.TICKER_REGEX;

@RestController
@RequestMapping("/api/funds")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/{ticker}/metrics")
    public MetricsResponse getMetrics(
            @PathVariable @Pattern(regexp = TICKER_REGEX, message = TICKER_MESSAGE) String ticker,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return metricsService.getMetrics(ticker, from, to);
    }
}
