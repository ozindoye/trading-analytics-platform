package com.ousman.trading_analytics_platform.controller;

import com.ousman.trading_analytics_platform.service.PriceIngestionService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Dev-only manual trigger for a full-history backfill (~5000 bars).
// Excluded from prod by @Profile, where PriceIngestionScheduler does incremental refreshes instead.
@Profile("!prod")
@RestController
@RequestMapping("/admin/ingest")
public class IngestionAdminController {

    private final PriceIngestionService priceIngestionService;

    public IngestionAdminController(PriceIngestionService priceIngestionService) {
        this.priceIngestionService = priceIngestionService;
    }

    @PostMapping("/{ticker}")
    public ResponseEntity<String> ingest(@PathVariable String ticker) {
        String symbol = ticker.toUpperCase();
        int saved = priceIngestionService.ingestDailyPrices(symbol, 5000);
        return ResponseEntity.ok("Ingested " + saved + " new bars for " + symbol);
    }
}