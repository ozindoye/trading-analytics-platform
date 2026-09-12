package com.ousman.trading_analytics_platform.dto;

import com.ousman.trading_analytics_platform.model.PriceBar;

public final class PriceBarMapper {

    private PriceBarMapper() {
    }

    public static PriceBarResponse toResponse(PriceBar bar) {
        return new PriceBarResponse(bar.getDate(), bar.getClose());
    }
}
