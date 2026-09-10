package com.ousman.trading_analytics_platform.config;

import com.ousman.trading_analytics_platform.model.Fund;
import com.ousman.trading_analytics_platform.repository.FundRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FundSeeder implements CommandLineRunner {

    private final FundRepository fundRepository;

    public FundSeeder(FundRepository fundRepository) {
        this.fundRepository = fundRepository;
    }

    @Override
    public void run(String... args) {
        seedFund("SPY", "SPDR S&P 500 ETF Trust");
        seedFund("QQQ", "Invesco QQQ Trust");
        seedFund("VTI", "Vanguard Total Stock Market ETF");
    }

    private void seedFund(String ticker, String name) {
        if (fundRepository.findByTicker(ticker).isEmpty()) {
            fundRepository.save(new Fund(ticker, name));
        }
    }
}