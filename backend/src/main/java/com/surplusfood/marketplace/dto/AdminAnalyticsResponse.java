package com.surplusfood.marketplace.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AdminAnalyticsResponse(
        long totalUsers,
        Map<String, Long> usersByRole,
        long totalListings,
        long totalOrders,
        BigDecimal globalRevenue,
        long globalWasteSavedItems,
        List<MonthlyChartData> globalListingTrend,
        List<MonthlyChartData> globalOrderTrend
) {
}
