package com.ousman.trading_analytics_platform.controller;

import com.ousman.trading_analytics_platform.dto.PriceBarResponse;
import com.ousman.trading_analytics_platform.service.PriceQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/funds")
public class PriceController {

    private final PriceQueryService priceQueryService;

    public PriceController(PriceQueryService priceQueryService) {
        this.priceQueryService = priceQueryService;
    }

    @GetMapping("/{ticker}/prices")
    public List<PriceBarResponse> getPrices(
            @PathVariable String ticker,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from) {
        return priceQueryService.getPriceHistory(ticker, from);
    }
}