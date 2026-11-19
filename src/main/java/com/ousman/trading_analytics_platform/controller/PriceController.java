package com.ousman.trading_analytics_platform.controller;

import com.ousman.trading_analytics_platform.dto.CompareRowResponse;
import com.ousman.trading_analytics_platform.dto.PriceBarResponse;
import com.ousman.trading_analytics_platform.service.PriceQueryService;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

// No @Validated on purpose: Spring MVC validates constrained parameters by itself
// and throws HandlerMethodValidationException, which GlobalExceptionHandler turns into a 400.
@RestController
@RequestMapping("/api/funds")
public class PriceController {

    // Every ticker in my universe is plain letters, so anything else is a bad request
    // and shouldn't get as far as a database lookup.
    static final String TICKER_REGEX = "[A-Za-z]{1,10}";
    static final String TICKER_MESSAGE = "ticker must be 1 to 10 letters";

    private final PriceQueryService priceQueryService;

    public PriceController(PriceQueryService priceQueryService) {
        this.priceQueryService = priceQueryService;
    }

    @GetMapping("/{ticker}/prices")
    public List<PriceBarResponse> getPrices(
            @PathVariable @Pattern(regexp = TICKER_REGEX, message = TICKER_MESSAGE) String ticker,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from) {
        return priceQueryService.getPriceHistory(ticker, from);
    }

    // The constraint sits on the element type so each ticker in the list is checked,
    // not the list itself.
    @GetMapping("/compare")
    public List<CompareRowResponse> compare(
            @RequestParam List<@Pattern(regexp = TICKER_REGEX, message = TICKER_MESSAGE) String> tickers,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from) {
        return priceQueryService.comparePerformance(tickers, from);
    }
}
