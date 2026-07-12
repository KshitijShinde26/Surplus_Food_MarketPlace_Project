# Step 5: NGO Profiles, Orders, Stripe Payments, and Donations

## What Was Added

Backend:

- NgoProfile, Order, Payment, and Donation JPA entities.
- OrderStatus, PaymentStatus, and DonationStatus enums.
- NGO profile management endpoints (POST, GET, PUT).
- Consumer checkout (Order placement) API.
- Stripe Sandbox PaymentIntent creation API.
- Public webhook endpoint to consume events from Stripe (success/failed).
- Donation claiming API for NGOs.
- Donation claim approval API for Business Owners.
- Multi-user race condition check via JPA Optimistic Locking (`@Version` on `FoodListing`).

Frontend:

- Services for NGO (`ngoService.js`), orders (`orderService.js`), payments (`paymentService.js`), and donations (`donationService.js`).

## Folder Structure

```text
backend/src/main/java/com/surplusfood/marketplace/
  config/
    SecurityConfig.java (modified)
  controller/
    AdminNgoController.java
    DonationController.java
    NgoProfileController.java
    OrderController.java
    PaymentController.java
  dto/
    DonationClaimRequest.java
    DonationResponse.java
    NgoProfileRequest.java
    NgoProfileResponse.java
    OrderRequest.java
    OrderResponse.java
    PaymentIntentResponse.java
  entity/
    Donation.java
    DonationStatus.java
    NgoProfile.java
    Order.java
    OrderStatus.java
    Payment.java
    PaymentStatus.java
  mapper/
    DonationMapper.java
    NgoProfileMapper.java
    OrderMapper.java
  repository/
    DonationRepository.java
    NgoProfileRepository.java
    OrderRepository.java
    PaymentRepository.java
  service/
    DonationService.java
    NgoService.java
    OrderService.java
    StripePaymentService.java

frontend/src/services/
  donationService.js
  ngoService.js
  orderService.js
  paymentService.js
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

### 1. Setup NGO Profile

Register a user with role `ROLE_NGO` and get access token:

```http
POST /api/ngo/profile
Content-Type: application/json
Authorization: Bearer NGO_JWT

{
  "organizationName": "Feeding Hands Foundation",
  "registrationNumber": "REG-FED-1002",
  "addressLine": "100 Relief Avenue",
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

Expected:
- HTTP 201 Created.
- Return profile details with `verified = false`.

### 2. Place Order (Discount Sale)

```http
POST /api/orders
Content-Type: application/json
Authorization: Bearer CONSUMER_JWT

{
  "listingId": 1,
  "quantity": 2
}
```

Expected:
- HTTP 201 Created.
- Total amount calculates dynamically from listing's discount price.
- `availableQuantity` on the listing decrements by 2.

### 3. Create Stripe PaymentIntent

```http
POST /api/payments/create-intent/1
Authorization: Bearer CONSUMER_JWT
```

Expected:
- HTTP 200 OK.
- Returns the client secret and a Stripe PaymentIntent ID (or mock sandbox indicators if `STRIPE_SECRET_KEY` is not set).

### 4. Claim Donation (Free Donation)

```http
POST /api/donations/claim
Content-Type: application/json
Authorization: Bearer NGO_JWT

{
  "listingId": 2,
  "quantity": 3
}
```

Expected:
- HTTP 201 Created.
- Status is `CLAIMED`.
- `availableQuantity` on the listing decrements by 3.

### 5. Approve NGO Claim (Business Owner)

```http
POST /api/donations/1/approve
Authorization: Bearer BUSINESS_OWNER_JWT
```

Expected:
- HTTP 200 OK.
- Donation status becomes `APPROVED`.

---

## Testing Checklist

1. NGOs must be verified by the admin (account status is `ACTIVE` and profile `verified` is true) before they can claim donations.
2. Orders can only be placed on active, unexpired food listings that are marked as `DISCOUNT_SALE`.
3. Donations can only be claimed on active, unexpired food listings that are marked as `FREE_DONATION`.
4. Placed orders or claimed donations deduct stock immediately.
5. If two threads try to claim/buy more than the available quantity simultaneously, JPA Optimistic Locking triggers a rollback and blocks the second transaction with a version mismatch error.

## Git Commit

```powershell
git add .
git commit -m "Implement Step 5: NGO profiles, orders, Stripe, donations, and locking"
```
