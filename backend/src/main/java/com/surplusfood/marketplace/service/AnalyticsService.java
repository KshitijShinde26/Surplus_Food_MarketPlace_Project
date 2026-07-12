package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.AdminAnalyticsResponse;
import com.surplusfood.marketplace.dto.BusinessAnalyticsResponse;
import com.surplusfood.marketplace.dto.MonthlyChartData;
import com.surplusfood.marketplace.dto.NgoAnalyticsResponse;
import com.surplusfood.marketplace.entity.Business;
import com.surplusfood.marketplace.entity.DonationStatus;
import com.surplusfood.marketplace.entity.NgoProfile;
import com.surplusfood.marketplace.entity.OrderStatus;
import com.surplusfood.marketplace.entity.TransactionStatus;
import com.surplusfood.marketplace.entity.TransactionType;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.repository.BusinessRepository;
import com.surplusfood.marketplace.repository.ComplaintRepository;
import com.surplusfood.marketplace.repository.DonationRepository;
import com.surplusfood.marketplace.repository.FoodListingRepository;
import com.surplusfood.marketplace.repository.NgoProfileRepository;
import com.surplusfood.marketplace.repository.OrderRepository;
import com.surplusfood.marketplace.repository.ReviewRepository;
import com.surplusfood.marketplace.repository.TransactionRepository;
import com.surplusfood.marketplace.repository.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final FoodListingRepository foodListingRepository;
    private final OrderRepository orderRepository;
    private final DonationRepository donationRepository;
    private final ReviewRepository reviewRepository;
    private final TransactionRepository transactionRepository;

    private static final String[] MONTHS = {
            "", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    @Transactional(readOnly = true)
    public BusinessAnalyticsResponse getBusinessAnalytics(Long ownerId) {
        Business business = businessRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found"));

        BigDecimal revenue = transactionRepository.calculateEarningsForBusiness(business.getId());
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        long donationsCount = donationRepository.countByListingBusinessIdAndStatusIn(
                business.getId(), List.of(DonationStatus.APPROVED, DonationStatus.PICKED_UP));

        long ordersSaved = orderRepository.sumQuantityByBusinessIdAndPaidStatus(business.getId());
        long donationsSaved = donationRepository.sumQuantityByBusinessIdAndApprovedStatus(business.getId());
        long totalWasteSaved = ordersSaved + donationsSaved;

        Double avgRatingVal = reviewRepository.getAverageRatingForBusiness(business.getId());
        double avgRating = avgRatingVal != null ? avgRatingVal : 0.0;

        List<Object[]> revenueRaw = transactionRepository.getMonthlyRevenueTrendForBusiness(business.getId());
        List<Object[]> donationsRaw = donationRepository.getMonthlyDonationsTrendForBusiness(business.getId());

        return new BusinessAnalyticsResponse(
                revenue,
                donationsCount,
                totalWasteSaved,
                avgRating,
                mapTrend(revenueRaw),
                mapTrend(donationsRaw)
        );
    }

    @Transactional(readOnly = true)
    public AdminAnalyticsResponse getAdminAnalytics() {
        long totalUsers = userRepository.count();

        List<Object[]> rawRoles = userRepository.countUsersByRole();
        Map<String, Long> rolesMap = new HashMap<>();
        for (Object[] row : rawRoles) {
            rolesMap.put((String) row[0], (Long) row[1]);
        }

        long totalListings = foodListingRepository.count();
        
        long totalOrders = orderRepository.countByStatusIn(
                List.of(OrderStatus.PAID, OrderStatus.ACCEPTED, OrderStatus.READY_FOR_PICKUP, OrderStatus.COMPLETED));

        BigDecimal globalRevenue = BigDecimal.ZERO;
        for (Object[] trend : transactionRepository.getGlobalMonthlyRevenueTrend()) {
            if (trend[1] != null) {
                globalRevenue = globalRevenue.add((BigDecimal) trend[1]);
            }
        }

        long globalOrdersQty = orderRepository.sumGlobalQuantityByPaidStatus();
        long globalDonationsQty = donationRepository.sumGlobalQuantityByApprovedStatus();
        long globalWasteSaved = globalOrdersQty + globalDonationsQty;

        List<Object[]> listingsRaw = foodListingRepository.getMonthlyListingsTrend();
        List<Object[]> ordersRaw = orderRepository.getMonthlyOrdersTrend();

        return new AdminAnalyticsResponse(
                totalUsers,
                rolesMap,
                totalListings,
                totalOrders,
                globalRevenue,
                globalWasteSaved,
                mapTrend(listingsRaw),
                mapTrend(ordersRaw)
        );
    }

    @Transactional(readOnly = true)
    public NgoAnalyticsResponse getNgoAnalytics(Long userId) {
        NgoProfile ngo = ngoProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("NGO profile not found"));

        long totalClaims = donationRepository.countByNgoId(ngo.getId());
        long activeClaims = donationRepository.countByNgoIdAndStatus(ngo.getId(), DonationStatus.CLAIMED);
        
        long completedClaims = donationRepository.countByNgoIdAndStatusIn(
                ngo.getId(), List.of(DonationStatus.APPROVED, DonationStatus.PICKED_UP));

        long itemsSecured = donationRepository.sumQuantityByNgoIdAndApprovedStatus(ngo.getId());

        List<Object[]> claimsRaw = donationRepository.getMonthlyClaimsTrendForNgo(ngo.getId());

        return new NgoAnalyticsResponse(
                totalClaims,
                activeClaims,
                completedClaims,
                itemsSecured,
                mapTrend(claimsRaw)
        );
    }

    private List<MonthlyChartData> mapTrend(List<Object[]> rawTrend) {
        List<MonthlyChartData> list = new ArrayList<>();
        if (rawTrend == null) {
            return list;
        }
        for (Object[] row : rawTrend) {
            Integer monthNum = (Integer) row[0];
            BigDecimal val = BigDecimal.ZERO;
            if (row[1] instanceof Long) {
                val = BigDecimal.valueOf((Long) row[1]);
            } else if (row[1] instanceof BigDecimal) {
                val = (BigDecimal) row[1];
            } else if (row[1] instanceof Double) {
                val = BigDecimal.valueOf((Double) row[1]);
            }
            String monthName = (monthNum != null && monthNum >= 1 && monthNum <= 12) ? MONTHS[monthNum] : "Unknown";
            list.add(new MonthlyChartData(monthName, val));
        }
        return list;
    }
}
