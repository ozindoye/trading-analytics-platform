package com.ousman.trading_analytics_platform.service;

import com.ousman.trading_analytics_platform.dto.PriceBarMapper;
import com.ousman.trading_analytics_platform.dto.PriceBarResponse;
import com.ousman.trading_analytics_platform.exception.ResourceNotFoundException;
import com.ousman.trading_analytics_platform.model.Fund;
import com.ousman.trading_analytics_platform.model.PriceBar;
import com.ousman.trading_analytics_platform.repository.FundRepository;
import com.ousman.trading_analytics_platform.repository.PriceBarRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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
}
