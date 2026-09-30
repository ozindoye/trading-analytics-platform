package com.ousman.trading_analytics_platform.service;

import com.ousman.trading_analytics_platform.dto.PriceBarMapper;
import com.ousman.trading_analytics_platform.dto.PriceBarResponse;
import com.ousman.trading_analytics_platform.exception.ResourceNotFoundException;
import com.ousman.trading_analytics_platform.model.Fund;
import com.ousman.trading_analytics_platform.model.PriceBar;
import com.ousman.trading_analytics_platform.repository.FundRepository;
import com.ousman.trading_analytics_platform.repository.PriceBarRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
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

    public List<Map<String, Object>> comparePerformance(List<String> tickers, LocalDate from) {
        TreeMap<LocalDate, Map<String, Object>> rowsByDate = new TreeMap<>();

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

                Map<String, Object> row = rowsByDate.computeIfAbsent(bar.getDate(), d -> {
                    Map<String, Object> newRow = new LinkedHashMap<>();
                    newRow.put("date", d);
                    return newRow;
                });
                row.put(ticker, pctChange);
            }
        }

        return new ArrayList<>(rowsByDate.values());
    }
}
