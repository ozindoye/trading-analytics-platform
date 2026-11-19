package com.ousman.trading_analytics_platform.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
