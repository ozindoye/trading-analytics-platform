package com.ousman.trading_analytics_platform.controller;

import com.ousman.trading_analytics_platform.dto.PriceBarResponse;
import com.ousman.trading_analytics_platform.service.PriceQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/funds")
public class PriceController {

    private final PriceQueryService priceQueryService;

    public PriceController(PriceQueryService priceQueryService) {
        this.priceQueryService = priceQueryService;
    }

    @GetMapping("/{ticker}/prices")
    public List<PriceBarResponse> getPrices(@PathVariable String ticker) {
        return priceQueryService.getPriceHistory(ticker);
    }
}