package com.ousman.trading_analytics_platform.service;

import com.ousman.trading_analytics_platform.client.TwelveDataClient;
import com.ousman.trading_analytics_platform.client.TwelveDataTimeSeriesResponse;
import com.ousman.trading_analytics_platform.client.TwelveDataValue;
import com.ousman.trading_analytics_platform.model.Fund;
import com.ousman.trading_analytics_platform.model.PriceBar;
import com.ousman.trading_analytics_platform.repository.FundRepository;
import com.ousman.trading_analytics_platform.repository.PriceBarRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// All times are written in UTC because that's what the production server runs on,
// which is exactly the case the New York conversion has to get right.
class PriceIngestionServiceTest {

    private static boolean complete(String barDate, String nowUtc) {
        return PriceIngestionService.isCompleteBar(LocalDate.parse(barDate), ZonedDateTime.parse(nowUtc));
    }

    @Test
    void rejectsTodaysBarWhileMarketIsOpen() {
        // 15:00 UTC on 18 Nov is 10:00 in New York
        assertFalse(complete("2026-11-18", "2026-11-18T15:00:00Z"));
    }

    @Test
    void acceptsTodaysBarAtTheScheduledRunInWinter() {
        // 22:00 UTC in November is 17:00 EST
        assertTrue(complete("2026-11-18", "2026-11-18T22:00:00Z"));
    }

    @Test
    void acceptsTodaysBarAtTheScheduledRunInSummer() {
        // 22:00 UTC in July is 18:00 EDT
        assertTrue(complete("2026-07-15", "2026-07-15T22:00:00Z"));
    }

    @Test
    void cutoffIsExactInWinter() {
        // 21:29 / 21:30 UTC is 16:29 / 16:30 EST
        assertFalse(complete("2026-11-18", "2026-11-18T21:29:00Z"));
        assertTrue(complete("2026-11-18", "2026-11-18T21:30:00Z"));
    }

    @Test
    void cutoffMovesWithDaylightSavingInSummer() {
        // 20:29 / 20:30 UTC is 16:29 / 16:30 EDT
        assertFalse(complete("2026-07-15", "2026-07-15T20:29:00Z"));
        assertTrue(complete("2026-07-15", "2026-07-15T20:30:00Z"));
    }

    @Test
    void acceptsYesterdaysBarEvenWhileMarketIsOpen() {
        assertTrue(complete("2026-11-17", "2026-11-18T15:00:00Z"));
    }

    @Test
    void usesTheNewYorkDateNotTheUtcDate() {
        // 02:00 UTC on 19 Nov is still 21:00 on 18 Nov in New York:
        // the 18th is finished, and a bar dated the 19th can't be real yet.
        assertTrue(complete("2026-11-18", "2026-11-19T02:00:00Z"));
        assertFalse(complete("2026-11-19", "2026-11-19T02:00:00Z"));
    }

    // The N+1 fix: stored dates are fetched once per fund, not once per incoming bar.
    @Test
    @SuppressWarnings("unchecked")
    void looksUpStoredDatesOnceAndSavesOnlyNewBars() {
        TwelveDataClient client = mock(TwelveDataClient.class);
        FundRepository fundRepository = mock(FundRepository.class);
        PriceBarRepository priceBarRepository = mock(PriceBarRepository.class);
        PriceIngestionService service = new PriceIngestionService(client, fundRepository, priceBarRepository);

        Fund spy = new Fund("SPY", "Test Fund");
        when(fundRepository.findByTicker("SPY")).thenReturn(Optional.of(spy));
        when(client.fetchDailySeries("SPY", 3)).thenReturn(new TwelveDataTimeSeriesResponse("ok", List.of(
                value("2025-01-06"),
                value("2025-01-03"),
                value("2025-01-02")
        )));
        when(priceBarRepository.findDatesByFund(spy))
                .thenReturn(Set.of(LocalDate.parse("2025-01-02"), LocalDate.parse("2025-01-03")));

        int saved = service.ingestDailyPrices("SPY", 3);

        assertEquals(1, saved);
        verify(priceBarRepository, times(1)).findDatesByFund(spy);
        ArgumentCaptor<List<PriceBar>> captor = ArgumentCaptor.forClass(List.class);
        verify(priceBarRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(LocalDate.parse("2025-01-06"), captor.getValue().get(0).getDate());
    }

    private static TwelveDataValue value(String date) {
        return new TwelveDataValue(date, "100", "101", "99", "100.5", "1000");
    }
}
