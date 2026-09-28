package com.ousman.trading_analytics_platform.service;

import com.ousman.trading_analytics_platform.dto.MetricsResponse;
import com.ousman.trading_analytics_platform.exception.ResourceNotFoundException;
import com.ousman.trading_analytics_platform.model.Fund;
import com.ousman.trading_analytics_platform.model.PriceBar;
import com.ousman.trading_analytics_platform.repository.FundRepository;
import com.ousman.trading_analytics_platform.repository.PriceBarRepository;
import com.ousman.trading_analytics_platform.service.MetricsCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MetricsService {

    private final FundRepository fundRepository;
    private final PriceBarRepository priceBarRepository;

    public MetricsService(FundRepository fundRepository,
                          PriceBarRepository priceBarRepository) {
        this.fundRepository = fundRepository;
        this.priceBarRepository = priceBarRepository;
    }

    @Transactional(readOnly = true)
    public MetricsResponse getMetrics(String ticker, LocalDate from, LocalDate to) {
        Fund fund = fundRepository.findByTicker(ticker)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No fund found with ticker " + ticker));

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "'from' (" + from + ") must not be after 'to' (" + to + ")");
        }

        LocalDate start = (from != null) ? from : LocalDate.of(1970, 1, 1);
        LocalDate end = (to != null) ? to : LocalDate.now();

        List<PriceBar> bars = priceBarRepository.findByFundAndDateBetweenOrderByDateAsc(fund, start, end);

        if (bars.size() < 2) {
            throw new ResourceNotFoundException(
                    "Not enough price data for " + ticker
                            + " in the requested range to compute metrics");
        }

        return MetricsCalculator.computeMetrics(ticker, bars);
    }
}
