package com.ousman.trading_analytics_platform.service;

import com.ousman.trading_analytics_platform.model.Fund;
import com.ousman.trading_analytics_platform.repository.FundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Profile("prod")
@Component
public class PriceIngestionScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceIngestionScheduler.class);

    private final FundRepository fundRepository;
    private final PriceIngestionService priceIngestionService;

    public PriceIngestionScheduler(FundRepository fundRepository,
                                   PriceIngestionService priceIngestionService) {
        this.fundRepository = fundRepository;
        this.priceIngestionService = priceIngestionService;
    }

    @Scheduled(initialDelayString = "PT30S", fixedDelayString = "PT24H")
    public void refreshAllFunds() {
        for (Fund fund : fundRepository.findAll()) {
            try {
                int saved = priceIngestionService.ingestDailyPrices(fund.getTicker());
                log.info("Scheduled ingest for {}: {} new bars saved", fund.getTicker(), saved);
            } catch (Exception e) {
                log.error("Scheduled ingest failed for {}: {}", fund.getTicker(), e.getMessage());
            }
        }
    }
}
