# Step 7: Reviews, Transactions Ledger, and Complaints

## What Was Added

Backend:

- Review, Transaction, and Complaint JPA entities.
- TransactionType, TransactionStatus, and ComplaintStatus enums.
- Reviews APIs (`POST /api/reviews`, `GET /api/reviews/business/{businessId}`, `GET /api/reviews/business/{businessId}/average`).
- Business transaction ledger APIs (`GET /api/transactions`, `GET /api/transactions/earnings`).
- Complaints reporting APIs (`POST /api/complaints`, `GET /api/complaints/me`).
- Admin complaints dashboard APIs (`GET /api/admin/complaints`, `PATCH /api/admin/complaints/{id}/status`).
- Service hooks mapping logs on Stripe payment success (Sale), claim approval (Donation), and order cancellation (Refund).

Frontend:

- Services for reviews (`reviewService.js`), transactions (`transactionService.js`), and complaints (`complaintService.js`).

## Folder Structure

```text
backend/src/main/java/com/surplusfood/marketplace/
  controller/
    AdminComplaintController.java
    ComplaintController.java
    ReviewController.java
    TransactionController.java
  dto/
    ComplaintRequest.java
    ComplaintResponse.java
    ReviewRequest.java
    ReviewResponse.java
    TransactionResponse.java
  entity/
    Complaint.java
    ComplaintStatus.java
    Review.java
    Transaction.java
    TransactionStatus.java
    TransactionType.java
  mapper/
    ComplaintMapper.java
    ReviewMapper.java
    TransactionMapper.java
  repository/
    ComplaintRepository.java
    ReviewRepository.java
    TransactionRepository.java (modified)
  service/
    ComplaintService.java
    DonationService.java (modified)
    OrderService.java (modified)
    ReviewService.java
    StripePaymentService.java (modified)
    TransactionService.java

frontend/src/services/
  complaintService.js
  reviewService.js
  transactionService.js
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

### 1. Submit Business Review

Consumer leaves feedback on completed order #1:

```http
POST /api/reviews
Content-Type: application/json
Authorization: Bearer CONSUMER_JWT

{
  "orderId": 1,
  "rating": 5,
  "comment": "Edible fresh bread, highly recommended!"
}
```

Expected: Return HTTP 201 Created and review response object.

### 2. Get Average Rating

Get average stars for business:

```http
GET /api/reviews/business/1/average
```

Expected: Returns numerical value (e.g. 5.0).

### 3. File Complaint

Filing a complaint against a listing:

```http
POST /api/complaints
Content-Type: application/json
Authorization: Bearer CONSUMER_JWT

{
  "businessId": 1,
  "listingId": 1,
  "subject": "Expired Food Listed",
  "description": "The item listed was already expired when purchased."
}
```

Expected: Return HTTP 201 Complaint Response.

### 4. Admin Search Complaints

Search open complaints:

```http
GET /api/admin/complaints?status=OPEN
Authorization: Bearer ADMIN_JWT
```

Expected: Returns paginated list containing the complaint.

---

## Testing Checklist

1. Reviews can only be left once per order.
2. Review ratings must be bounded between 1 and 5.
3. Stripe sandbox payments and NGO approvals must automatically log transactions in the ledger history.
4. Business earnings reflect sales minus refunds.

## Git Commit

```powershell
git add .
git commit -m "Implement Step 7: Reviews, transaction ledgers, and complaints"
```
