package com.surplusfood.marketplace.dto;

import java.math.BigDecimal;
import java.util.List;

public record BusinessAnalyticsResponse(
        BigDecimal totalRevenue,
        long totalDonations,
        long wasteSavedItems,
        double averageRating,
        List<MonthlyChartData> revenueTrend,
        List<MonthlyChartData> donationTrend
) {
}
