package com.ousman.trading_analytics_platform.service;

import com.ousman.trading_analytics_platform.dto.CompareRowResponse;
import com.ousman.trading_analytics_platform.dto.PriceBarMapper;
import com.ousman.trading_analytics_platform.dto.PriceBarResponse;
import com.ousman.trading_analytics_platform.exception.ResourceNotFoundException;
import com.ousman.trading_analytics_platform.model.Fund;
import com.ousman.trading_analytics_platform.model.PriceBar;
import com.ousman.trading_analytics_platform.repository.FundRepository;
import com.ousman.trading_analytics_platform.repository.PriceBarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PriceQueryService {

    private final FundRepository fundRepository;
    private final PriceBarRepository priceBarRepository;

    public PriceQueryService(FundRepository fundRepository,
                             PriceBarRepository priceBarRepository) {
        this.fundRepository = fundRepository;
        this.priceBarRepository = priceBarRepository;
    }

    @Transactional(readOnly = true)
    public List<PriceBarResponse> getPriceHistory(String ticker, LocalDate from) {
        Fund fund = fundRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No fund found with ticker: " + ticker.toUpperCase()));

        List<PriceBar> bars = (from == null)
                ? priceBarRepository.findByFundOrderByDateAsc(fund)
                : priceBarRepository.findByFundAndDateGreaterThanEqualOrderByDateAsc(fund, from);

        return bars.stream()
                .map(PriceBarMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompareRowResponse> comparePerformance(List<String> tickers, LocalDate from) {
        // TreeMap keeps the rows in date order; the inner LinkedHashMap keeps tickers
        // in the order they were requested, so the chart legend matches the user's selection.
        TreeMap<LocalDate, Map<String, BigDecimal>> changesByDate = new TreeMap<>();

        for (String rawTicker : tickers) {
            String ticker = rawTicker.trim().toUpperCase();
            Fund fund = fundRepository.findByTicker(ticker)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No fund found with ticker: " + ticker));

            List<PriceBar> bars = (from == null)
                    ? priceBarRepository.findByFundOrderByDateAsc(fund)
                    : priceBarRepository.findByFundAndDateGreaterThanEqualOrderByDateAsc(fund, from);

            if (bars.isEmpty()) {
                continue;
            }

            BigDecimal firstClose = bars.get(0).getClose();

            for (PriceBar bar : bars) {
                BigDecimal pctChange = bar.getClose()
                        .subtract(firstClose)
                        .divide(firstClose, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

                changesByDate.computeIfAbsent(bar.getDate(), d -> new LinkedHashMap<>())
                        .put(ticker, pctChange);
            }
        }

        return changesByDate.entrySet().stream()
                .map(entry -> new CompareRowResponse(entry.getKey(), entry.getValue()))
                .toList();
    }
}
