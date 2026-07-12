# Step 4: Food Listing and Categories Management

## What Was Added

Backend:

- Category, FoodListing, and FoodListingImage JPA entities.
- ListingType enum (`DISCOUNT_SALE`, `FREE_DONATION`).
- FoodListingStatus enum (`ACTIVE`, `SOLD_OUT`, `EXPIRED`, `REMOVED`).
- Category and FoodListing JPA repositories.
- Custom radius search using the **Haversine formula** in native SQL.
- Category read API (`GET /api/categories`) public access.
- Image upload to Cloudinary with an automatic fallback mock configuration.
- Food listing CRUD APIs for Business Owners (pre-authorized).
- Public food listing search and radius filtering APIs.
- Pagination handling mapped from Spring Pageable.
- Input validation on requests.

Frontend:

- Services for category (`categoryService.js`), image upload (`imageService.js`), and listing requests (`foodListingService.js`).

## Folder Structure

```text
backend/src/main/java/com/surplusfood/marketplace/
  config/
    SecurityConfig.java (modified)
  controller/
    CategoryController.java
    FoodListingController.java
    ImageUploadController.java
  dto/
    CategoryResponse.java
    FoodListingImageRequest.java
    FoodListingImageResponse.java
    FoodListingRequest.java
    FoodListingResponse.java
  entity/
    Category.java
    FoodListing.java
    FoodListingImage.java
    FoodListingStatus.java
    ListingType.java
  mapper/
    FoodListingMapper.java
  repository/
    CategoryRepository.java
    FoodListingRepository.java
  service/
    CategoryService.java
    CloudinaryService.java
    FoodListingService.java

frontend/src/services/
  categoryService.js
  foodListingService.js
  imageService.js
```

## Dependencies

Already present in `backend/pom.xml`:
- `cloudinary-http44`

Already present in `frontend/package.json`:
- `axios`

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

### 1. View Categories

```http
GET /api/categories
```

Expected: Return a JSON array containing Bakery, Prepared Meals, Groceries, etc.

### 2. Create Food Listing (Business Owner)

Ensure you use a valid JWT representing a business owner whose business profile is verified (`verified = true`) and account is `ACTIVE`.

```http
POST /api/food-listings
Content-Type: application/json
Authorization: Bearer BUSINESS_OWNER_JWT

{
  "name": "Fresh Sourdough Bread",
  "description": "Artisanal bakery sourdough bread baked today.",
  "categoryId": 1,
  "quantity": 10,
  "originalPrice": 8.00,
  "discountPrice": 3.00,
  "listingType": "DISCOUNT_SALE",
  "vegetarian": true,
  "vegan": true,
  "expiryTime": "2026-12-31T22:00:00",
  "pickupStartTime": "2026-12-31T18:00:00",
  "pickupEndTime": "2026-12-31T21:00:00",
  "latitude": 28.6139,
  "longitude": 77.2090,
  "images": [
    {
      "imageUrl": "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
      "cloudinaryPublicId": "mock-sourdough",
      "sortOrder": 0
    }
  ]
}
```

Expected:
- HTTP 201 Created.
- Return payload mapping `availableQuantity` same as `quantity` (10), `status = ACTIVE`.

### 3. Update Own Food Listing

```http
PUT /api/food-listings/1
Content-Type: application/json
Authorization: Bearer BUSINESS_OWNER_JWT

{
  "name": "Fresh Sourdough Bread - Updated",
  "description": "Artisanal bakery sourdough bread baked today. Limited stock.",
  "categoryId": 1,
  "quantity": 12,
  "originalPrice": 8.00,
  "discountPrice": 2.50,
  "listingType": "DISCOUNT_SALE",
  "vegetarian": true,
  "vegan": true,
  "expiryTime": "2026-12-31T22:00:00",
  "pickupStartTime": "2026-12-31T18:00:00",
  "pickupEndTime": "2026-12-31T21:00:00",
  "latitude": 28.6139,
  "longitude": 77.2090,
  "images": [
    {
      "imageUrl": "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
      "cloudinaryPublicId": "mock-sourdough",
      "sortOrder": 0
    }
  ]
}
```

Expected:
- HTTP 200 OK.
- Quantity increases to 12. Available quantity adjusts to 12.

### 4. Search Listings by Location & Geoposition (Radius Search)

```http
GET /api/food-listings?latitude=28.6140&longitude=77.2091&radius=5&keyword=sourdough
```

Expected:
- HTTP 200 OK.
- Returns list containing the sourdough listing, ordered by distance (meters/kilometers) using the Haversine formula.

### 5. Soft-Delete Food Listing

```http
DELETE /api/food-listings/1
Authorization: Bearer BUSINESS_OWNER_JWT
```

Expected:
- Listing status sets to `REMOVED`. It will no longer show in public searches.

---

## Testing Checklist

1. Business owner must have a verified profile to list food. Unverified business owners should receive `403 Forbidden` with a message.
2. Creating a listing validates listing inputs (e.g. name length limits, expiry time in future).
3. The public `/api/food-listings` search allows radius search if both `latitude` and `longitude` are present, otherwise falls back to a query without distance ordering.
4. Business owners can delete listings, which changes the status to `REMOVED` in the database without destroying audit trail records.
5. Optimistic locking `@Version` increases automatically on edits.

## Git Commit

```powershell
git add .
git commit -m "Implement Step 4: Food Listing and Categories management"
```
