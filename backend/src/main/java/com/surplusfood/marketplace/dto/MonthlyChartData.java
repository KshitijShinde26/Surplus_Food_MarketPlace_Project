package com.surplusfood.marketplace.dto;

import java.math.BigDecimal;

public record MonthlyChartData(
        String label,
        BigDecimal value
) {
}
