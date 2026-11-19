package com.ousman.trading_analytics_platform.controller;

import com.ousman.trading_analytics_platform.dto.CompareRowResponse;
import com.ousman.trading_analytics_platform.service.PriceQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Goes through Spring's real JSON serialisation, so it checks the actual API contract
// the frontend depends on, not just the Java types.
@WebMvcTest(PriceController.class)
class PriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceQueryService priceQueryService;

    @Test
    void compareKeepsTheFlatRowShapeTheChartExpects() throws Exception {
        Map<String, BigDecimal> changes = new LinkedHashMap<>();
        changes.put("SPY", new BigDecimal("1.25"));
        changes.put("QQQ", new BigDecimal("-0.50"));
        when(priceQueryService.comparePerformance(anyList(), any()))
                .thenReturn(List.of(new CompareRowResponse(LocalDate.parse("2025-01-02"), changes)));

        mockMvc.perform(get("/api/funds/compare").param("tickers", "SPY,QQQ"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"date\":\"2025-01-02\",\"SPY\":1.25,\"QQQ\":-0.50}]", true));
    }

    @Test
    void compareRejectsAMalformedTickerWithTheStandardErrorBody() throws Exception {
        mockMvc.perform(get("/api/funds/compare").param("tickers", "SPY,b@d"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("ticker must be 1 to 10 letters"));
    }
}
