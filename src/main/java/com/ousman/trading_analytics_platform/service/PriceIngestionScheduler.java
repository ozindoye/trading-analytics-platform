package com.ousman.trading_analytics_platform.service;

import com.ousman.trading_analytics_platform.model.Fund;
import com.ousman.trading_analytics_platform.repository.FundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Profile("prod")
@Component
public class PriceIngestionScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceIngestionScheduler.class);

    private static final int REFRESH_BARS = 30;

    private final FundRepository fundRepository;
    private final PriceIngestionService priceIngestionService;

    public PriceIngestionScheduler(FundRepository fundRepository,
                                   PriceIngestionService priceIngestionService) {
        this.fundRepository = fundRepository;
        this.priceIngestionService = priceIngestionService;
    }

    // ApplicationReadyEvent fires after every CommandLineRunner, so FundSeeder has already
    // created the funds by the time this runs. A deploy during market hours is fine now,
    // because the ingestion service refuses today's bar until it's final.
    @EventListener(ApplicationReadyEvent.class)
    public void refreshOnStartup() {
        log.info("Startup ingest triggered");
        refreshAllFunds();
    }

    // 22:00 UTC is 17:00 in New York in winter and 18:00 in summer, so it always lands
    // after the bar cutoff. A fixed time also stops the schedule drifting with each deploy.
    @Scheduled(cron = "0 0 22 * * *", zone = "UTC")
    public void refreshAllFunds() {
        for (Fund fund : fundRepository.findAll()) {
            try {
                int saved = priceIngestionService.ingestDailyPrices(fund.getTicker(), REFRESH_BARS);
                log.info("Scheduled ingest for {}: {} new bars saved", fund.getTicker(), saved);
            } catch (Exception e) {
                log.error("Scheduled ingest failed for {}: {}", fund.getTicker(), e.getMessage());
            }
        }
    }
}
