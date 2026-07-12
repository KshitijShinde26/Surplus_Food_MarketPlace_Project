# Step 3: Business Profile and Verification Flow

## What Was Added

Backend:

- Business profile entity and business type enum.
- Business profile create, read, and update APIs for business owners.
- Admin APIs to search, verify, block, or mark businesses pending.
- Pagination response wrapper.
- Business mapper and repository.
- Business-owner account status changes:
  - New business profile starts as unverified.
  - Business update returns the account to `PENDING_VERIFICATION`.
  - Admin verification sets the owner account to `ACTIVE`.
  - Admin blocking sets the owner account to `BLOCKED`.
- Existing JWTs are rejected after an account is blocked.

Frontend:

- Business API service for owner profile and admin verification actions.

Database:

- Unique business owner index so one owner has one business profile in this phase.

## Folder Structure

```text
backend/src/main/java/com/surplusfood/marketplace/
  controller/
    AdminBusinessController.java
    BusinessController.java
  dto/
    BusinessProfileRequest.java
    BusinessResponse.java
    PageResponse.java
  entity/
    Business.java
    BusinessType.java
  mapper/
    BusinessMapper.java
  repository/
    BusinessRepository.java
  service/
    BusinessService.java
  util/
    PageMapper.java

frontend/src/services/
  businessService.js
```

## Dependencies

No new dependencies were required for Step 3.

## Database Setup

Fresh setup:

```powershell
mysql -u root -p < database\schema.sql
```

If you already ran Step 1 or Step 2 schema, add only this index:

```sql
USE surplus_food_marketplace;
CREATE UNIQUE INDEX uk_business_owner ON businesses(owner_id);
```

## Commands To Run

Backend:

```powershell
cd C:\Users\kshit\eclipse-workspace\Project\backend
mvn test
mvn spring-boot:run
```

Frontend:

```powershell
cd C:\Users\kshit\eclipse-workspace\Project\frontend
npm install
npm run build
npm run dev
```

If PowerShell blocks `npm`, use:

```powershell
npm.cmd install
npm.cmd run dev
```

## API Testing

Base URL:

```text
http://localhost:8080/api
```

### 1. Register Business Owner

```http
POST /api/auth/register
Content-Type: application/json

{
  "fullName": "Neha Sharma",
  "email": "neha.business@example.com",
  "phone": "+919999999001",
  "password": "Password123",
  "role": "ROLE_BUSINESS_OWNER",
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

Expected:

- User account status is `PENDING_VERIFICATION`.
- Response includes access token and refresh token.

### 2. Create Business Profile

```http
POST /api/business/profile
Content-Type: application/json
Authorization: Bearer BUSINESS_OWNER_ACCESS_TOKEN

{
  "businessName": "Fresh Basket Bakery",
  "businessType": "BAKERY",
  "licenseNumber": "DL-FOOD-2026-001",
  "addressLine": "12 Green Market Road",
  "city": "New Delhi",
  "state": "Delhi",
  "postalCode": "110001",
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

Expected:

- `verified` is `false`.
- Owner status remains `PENDING_VERIFICATION`.

### 3. View Own Business Profile

```http
GET /api/business/profile/me
Authorization: Bearer BUSINESS_OWNER_ACCESS_TOKEN
```

### 4. Update Own Business Profile

```http
PUT /api/business/profile/me
Content-Type: application/json
Authorization: Bearer BUSINESS_OWNER_ACCESS_TOKEN

{
  "businessName": "Fresh Basket Bakery & Cafe",
  "businessType": "CAFE",
  "licenseNumber": "DL-FOOD-2026-001",
  "addressLine": "12 Green Market Road",
  "city": "New Delhi",
  "state": "Delhi",
  "postalCode": "110001",
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

Expected:

- Profile updates successfully.
- `verified` becomes `false` again.
- Owner account returns to `PENDING_VERIFICATION`.

## Admin Testing

Admin self-registration is blocked by design. For local testing, register a normal user, then promote it in MySQL:

```sql
USE surplus_food_marketplace;

UPDATE users
SET account_status = 'ACTIVE'
WHERE email = 'admin@example.com';

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.email = 'admin@example.com';
```

Then log in as that user again to get a JWT with `ROLE_ADMIN`.

### 5. Search Businesses As Admin

```http
GET /api/admin/businesses?verified=false&page=0&size=20
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

Optional filters:

```text
verified=true
verified=false
keyword=bakery
page=0
size=20
```

### 6. Verify Business

```http
PATCH /api/admin/businesses/1/verify
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

Expected:

- Business `verified` becomes `true`.
- Owner account status becomes `ACTIVE`.

### 7. Block Fake Business

```http
PATCH /api/admin/businesses/1/block
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

Expected:

- Business `verified` becomes `false`.
- Owner account status becomes `BLOCKED`.
- Existing JWT access for that owner is rejected on the next protected request.

### 8. Mark Business Pending

```http
PATCH /api/admin/businesses/1/pending
Authorization: Bearer ADMIN_ACCESS_TOKEN
```

Expected:

- Business `verified` becomes `false`.
- Owner account status becomes `PENDING_VERIFICATION`.

## Frontend Integration

Business API methods are centralized in:

```text
frontend/src/services/businessService.js
```

Available functions:

- `createBusinessProfile(payload)`
- `getMyBusinessProfile()`
- `updateMyBusinessProfile(payload)`
- `searchBusinesses(params)`
- `verifyBusiness(businessId)`
- `blockBusiness(businessId)`
- `markBusinessPending(businessId)`

Screens for these flows will be added when we build the business and admin dashboards.

## Testing Checklist

1. Business owner can create one profile.
2. Creating a second profile returns `409 Conflict`.
3. Consumer token cannot call business profile APIs.
4. Business owner token cannot call admin APIs.
5. Admin token can list and verify businesses.
6. Verified business owner becomes `ACTIVE`.
7. Blocked business owner cannot continue using protected APIs.

## Git Commit

Suggested commit:

```powershell
git add .
git commit -m "Add business profile and verification flow"
```
