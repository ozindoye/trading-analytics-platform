package com.ousman.trading_analytics_platform.controller;   // (1) CHANGE to your real package

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ousman.trading_analytics_platform.dto.MetricsResponse;      // (2) your dto package
import com.ousman.trading_analytics_platform.service.MetricsService;   // (3) your service package

@RestController
@RequestMapping("/api/funds")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/{ticker}/metrics")
    public MetricsResponse getMetrics(
            @PathVariable String ticker,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return metricsService.getMetrics(ticker, from, to);
    }
}