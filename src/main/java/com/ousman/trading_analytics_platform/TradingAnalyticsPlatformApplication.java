package com.ousman.trading_analytics_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class TradingAnalyticsPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradingAnalyticsPlatformApplication.class, args);
	}

}
