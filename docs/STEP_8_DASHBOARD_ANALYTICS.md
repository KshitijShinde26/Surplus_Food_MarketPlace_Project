# Step 8: Dashboard Analytics and Trend Metrics

## What Was Added

Backend:

- MonthlyChartData, BusinessAnalyticsResponse, AdminAnalyticsResponse, and NgoAnalyticsResponse DTO classes.
- Analytics endpoints (`GET /api/analytics/business`, `GET /api/analytics/admin`, `GET /api/analytics/ngo`).
- Optimization queries for counting registered user counts grouped by roles.
- Aggregate queries for calculating food items saved (sold quantities plus approved donation quantities).
- Trend queries mapping listing volumes, completed purchases, NGO claims, and revenue earnings grouped by month numbers.

Frontend:

- Service connector (`analyticsService.js`) returning dashboard aggregates.

## Folder Structure

```text
backend/src/main/java/com/surplusfood/marketplace/
  controller/
    AnalyticsController.java
  dto/
    AdminAnalyticsResponse.java
    BusinessAnalyticsResponse.java
    MonthlyChartData.java
    NgoAnalyticsResponse.java
  repository/
    DonationRepository.java (modified)
    FoodListingRepository.java (modified)
    OrderRepository.java (modified)
    TransactionRepository.java (modified)
    UserRepository.java (modified)
  service/
    AnalyticsService.java

frontend/src/services/
  analyticsService.js
```

## Commands To Run

Backend:

```powershell
cd C:\Users\kshit\eclipse-workspace\Project\backend
mvn clean compile
mvn spring-boot:run
```

Frontend:

```powershell
cd C:\Users\kshit\eclipse-workspace\Project\frontend
npm run build
npm run dev
```

---

## API Testing

Base URL:

```text
http://localhost:8080/api
```

### 1. Business Owner Analytics

```http
GET /api/analytics/business
Authorization: Bearer BUSINESS_OWNER_JWT
```

Expected: Returns numerical sums of net sales, NGO donations, total food items saved, and average star ratings, accompanied by monthly trend arrays.

### 2. Administrator Analytics

```http
GET /api/analytics/admin
Authorization: Bearer ADMIN_JWT
```

Expected: Returns global system metrics including registered user roles distribution mapping, system-wide active/expired listing counts, total orders, and global earnings.

### 3. NGO Analytics

```http
GET /api/analytics/ngo
Authorization: Bearer NGO_JWT
```

Expected: Returns NGO metrics including total claims count, active claims, completed claims (approved or picked up), and count of food items secured.

---

## Testing Checklist

1. Verify that "Waste Saved" value aggregates both paid orders quantity and approved donation claim quantities.
2. Confirm the month index values (1 to 12) from database queries are correctly mapped into month names (January to December).
3. Confirm that users receive a `403 Forbidden` if they attempt to request stats for a role different from their own.

## Git Commit

```powershell
git add .
git commit -m "Implement Step 8: Dashboard analytics endpoints and chart trend data structures"
```
