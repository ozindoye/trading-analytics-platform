package com.ousman.trading_analytics_platform.service;

import com.ousman.trading_analytics_platform.client.TwelveDataClient;
import com.ousman.trading_analytics_platform.client.TwelveDataTimeSeriesResponse;
import com.ousman.trading_analytics_platform.client.TwelveDataValue;
import com.ousman.trading_analytics_platform.exception.ResourceNotFoundException;
import com.ousman.trading_analytics_platform.model.Fund;
import com.ousman.trading_analytics_platform.model.PriceBar;
import com.ousman.trading_analytics_platform.repository.FundRepository;
import com.ousman.trading_analytics_platform.repository.PriceBarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PriceIngestionService {

    private final TwelveDataClient twelveDataClient;
    private final FundRepository fundRepository;
    private final PriceBarRepository priceBarRepository;

    public PriceIngestionService(TwelveDataClient twelveDataClient,
                                 FundRepository fundRepository,
                                 PriceBarRepository priceBarRepository) {
        this.twelveDataClient = twelveDataClient;
        this.fundRepository = fundRepository;
        this.priceBarRepository = priceBarRepository;
    }

    @Transactional
    public int ingestDailyPrices(String ticker, int outputSize) {
        Fund fund = fundRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No fund found with ticker: " + ticker.toUpperCase()));

        TwelveDataTimeSeriesResponse response = twelveDataClient.fetchDailySeries(ticker, outputSize);

        if (response == null || !"ok".equals(response.status()) || response.values() == null) {
            throw new IllegalStateException("Twelve Data returned no usable data for " + ticker);
        }

        List<PriceBar> newBars = new ArrayList<>();
        for (TwelveDataValue value : response.values()) {
            LocalDate date = LocalDate.parse(value.datetime());

            if (priceBarRepository.existsByFundAndDate(fund, date)) {
                continue;
            }

            newBars.add(new PriceBar(
                    fund,
                    date,
                    new BigDecimal(value.open()),
                    new BigDecimal(value.high()),
                    new BigDecimal(value.low()),
                    new BigDecimal(value.close()),
                    Long.parseLong(value.volume())
            ));
        }

        priceBarRepository.saveAll(newBars);
        return newBars.size();
    }
}