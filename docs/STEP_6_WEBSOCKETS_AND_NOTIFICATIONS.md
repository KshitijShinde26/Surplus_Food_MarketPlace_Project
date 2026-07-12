# Step 6: WebSockets, Real-time Alerts, Notifications, and Wishlist

## What Was Added

Backend:

- WishlistId, Wishlist, Notification, and PickupSchedule JPA entities.
- NotificationType and PickupStatus enums.
- Wishlist operations APIs (`POST /api/wishlist/{businessId}`, `DELETE /api/wishlist/{businessId}`, `GET /api/wishlist/me`).
- In-app notification API (`GET /api/notifications`, `PATCH /api/notifications/{id}/read`, `POST /api/notifications/read-all`).
- Pickup schedule details APIs.
- Asynchronous SMTP email sender (`EmailService.java`) with a connection failure wrapper.
- STOMP WebSocket broker configuration on `/ws`.
- Automated Cron Job Scheduler (`NotificationScheduler.java`) checking expired active listings and reminding users of upcoming pickups.
- Hook triggers pushing alerts when:
  - Business creates food (alerts nearby users + wishlisters, broadcasts to WebSocket topic).
  - Consumer checkout completed (alerts business, alerts consumer).
  - Webhook payment succeeded (alerts business, alerts consumer, schedules pickup).
  - NGO claims donation (alerts business).
  - Business approves claim (alerts NGO, schedules pickup).

Frontend:

- Services for wishlist (`wishlistService.js`) and notifications (`notificationService.js`).

## Folder Structure

```text
backend/src/main/java/com/surplusfood/marketplace/
  config/
    NotificationScheduler.java
    SecurityConfig.java (modified)
    WebSocketConfig.java
  controller/
    NotificationController.java
    PickupScheduleController.java
    WishlistController.java
  dto/
    NotificationResponse.java
    PickupScheduleResponse.java
  entity/
    Notification.java
    NotificationType.java
    PickupSchedule.java
    PickupStatus.java
    Wishlist.java
    WishlistId.java
  repository/
    FoodListingRepository.java (modified)
    NotificationRepository.java
    PickupScheduleRepository.java
    UserRepository.java (modified)
    WishlistRepository.java
  service/
    DonationService.java (modified)
    EmailService.java
    FoodListingService.java (modified)
    NotificationService.java
    OrderService.java (modified)
    PickupScheduleService.java
    StripePaymentService.java (modified)
  SurplusFoodMarketplaceApplication.java (modified)

frontend/src/services/
  notificationService.js
  wishlistService.js
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

### 1. Wishlist a Business

As a logged-in Consumer:

```http
POST /api/wishlist/1
Authorization: Bearer CONSUMER_JWT
```

Expected: Return JSON mapping successfully added.

### 2. View My Wishlist

```http
GET /api/wishlist/me
Authorization: Bearer CONSUMER_JWT
```

Expected: Returns the wishlisted business details in an array.

### 3. Check Realtime Websockets

Connect a STOMP client to:
`ws://localhost:8080/api/ws` (or with SockJS mapping `http://localhost:8080/api/ws`).

Subscribe to:
- Broadcasts: `/topic/listings` (new listings broadcast to all users).
- Personal notifications: `/user/queue/notifications` (user-specific updates).

When the wishlisted business owner creates a listing, confirm the STOMP subscriber receives the payload and an entry is added to `/api/notifications`.

### 4. Fetch User Notifications

```http
GET /api/notifications
Authorization: Bearer CONSUMER_JWT
```

Expected: Returns paginated in-app alerts showing `NEW_FOOD_NEARBY`.

### 5. Mark All Read

```http
POST /api/notifications/read-all
Authorization: Bearer CONSUMER_JWT
```

Expected: All notifications update to `readAt != null`.

---

## Testing Checklist

1. Adding/removing items from wishlist updates database wishlist composite keys.
2. WebSockets broker supports both raw WebSockets and SockJS fallbacks.
3. If SMTP connection fails, background alerts catch the exception and do not interrupt API request execution.
4. Auto-expiry cron scan triggers listing state updates to `EXPIRED` once they pass their expiry date.

## Git Commit

```powershell
git add .
git commit -m "Implement Step 6: WebSockets, real-time alerts, cron schedulers, and wishlists"
```
