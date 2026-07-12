package com.surplusfood.marketplace.dto;

import java.util.List;

public record NgoAnalyticsResponse(
        long totalClaims,
        long activeClaims,
        long completedClaims,
        long foodItemsSecured,
        List<MonthlyChartData> claimsTrend
) {
}
