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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PriceIngestionService {

    // All my funds are US-listed, so "today" and "after the close" have to be judged
    // on the exchange's clock, not the server's (Railway runs in UTC).
    static final ZoneId MARKET_ZONE = ZoneId.of("America/New_York");

    // The close is 16:00; I give the provider half an hour to publish the final daily bar.
    static final LocalTime BAR_FINAL_AFTER = LocalTime.of(16, 30);

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

        ZonedDateTime now = ZonedDateTime.now(MARKET_ZONE);

        List<PriceBar> newBars = new ArrayList<>();
        for (TwelveDataValue value : response.values()) {
            LocalDate date = LocalDate.parse(value.datetime());

            // A bar stored mid-session would never be corrected, because existing dates are
            // skipped on every later run. Leaving it out means the next run picks it up complete.
            if (!isCompleteBar(date, now)) {
                continue;
            }

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

    // Static and given "now" as an argument so I can test every case with fixed times,
    // without waiting for a real market close.
    static boolean isCompleteBar(LocalDate barDate, ZonedDateTime now) {
        ZonedDateTime marketNow = now.withZoneSameInstant(MARKET_ZONE);
        LocalDate marketToday = marketNow.toLocalDate();

        if (barDate.isBefore(marketToday)) {
            return true;
        }
        if (barDate.isAfter(marketToday)) {
            return false;
        }
        return !marketNow.toLocalTime().isBefore(BAR_FINAL_AFTER);
    }
}
