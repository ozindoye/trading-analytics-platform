package com.ousman.trading_analytics_platform.service;

import com.ousman.trading_analytics_platform.dto.MetricsResponse;
import com.ousman.trading_analytics_platform.model.PriceBar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class MetricsCalculator {

    private static final double TRADING_DAYS_PER_YEAR = 252.0;
    private static final double RISK_FREE_RATE = 0.0; // documented simplification; a real Sharpe uses the T-bill rate

    private MetricsCalculator() {
    }

    public static MetricsResponse computeMetrics(String ticker, List<PriceBar> bars) {
        if (bars.size() < 2) {
            throw new IllegalArgumentException(
                    "Need at least 2 price bars to compute metrics for " + ticker);
        }

        PriceBar first = bars.get(0);
        PriceBar last = bars.get(bars.size() - 1);
        BigDecimal startClose = first.getClose();
        BigDecimal endClose = last.getClose();

        BigDecimal periodReturnPct = endClose.subtract(startClose)
                .divide(startClose, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        int count = bars.size() - 1;
        double[] dailyReturns = new double[count];
        double sum = 0.0;
        for (int i = 1; i < bars.size(); i++) {
            double prev = bars.get(i - 1).getClose().doubleValue();
            double curr = bars.get(i).getClose().doubleValue();
            double r = (curr / prev) - 1.0;
            dailyReturns[i - 1] = r;
            sum += r;
        }

        double mean = sum / count;
        double sumSquaredDiffs = 0.0;
        for (double r : dailyReturns) {
            sumSquaredDiffs += Math.pow(r - mean, 2);
        }
        double variance = count > 1 ? sumSquaredDiffs / (count - 1) : 0.0;
        double dailyStdDev = Math.sqrt(variance);
        double annualisedVol = dailyStdDev * Math.sqrt(TRADING_DAYS_PER_YEAR) * 100.0;
        BigDecimal annualisedVolatilityPct = BigDecimal.valueOf(annualisedVol)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal high = bars.stream().map(PriceBar::getHigh).max(BigDecimal::compareTo).orElseThrow();
        BigDecimal low = bars.stream().map(PriceBar::getLow).min(BigDecimal::compareTo).orElseThrow();

        // Sharpe ratio (annualised): average return per unit of risk. rf assumed 0.
        double sharpe = dailyStdDev == 0.0
                ? 0.0
                : (mean - RISK_FREE_RATE / TRADING_DAYS_PER_YEAR) / dailyStdDev * Math.sqrt(TRADING_DAYS_PER_YEAR);
        BigDecimal sharpeRatio = BigDecimal.valueOf(sharpe).setScale(2, RoundingMode.HALF_UP);

        // Maximum drawdown: the worst peak-to-trough fall over the window.
        double peak = bars.get(0).getClose().doubleValue();
        double maxDrawdown = 0.0;
        for (PriceBar bar : bars) {
            double close = bar.getClose().doubleValue();
            if (close > peak) {
                peak = close;
            }
            double drawdown = (peak - close) / peak;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }
        BigDecimal maxDrawdownPct = BigDecimal.valueOf(maxDrawdown * 100).setScale(2, RoundingMode.HALF_UP);

        return new MetricsResponse(ticker, first.getDate(), last.getDate(),
                startClose, endClose, periodReturnPct, annualisedVolatilityPct,
                maxDrawdownPct, sharpeRatio, high, low);
    }
}
