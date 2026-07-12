# Step 1: Project Setup

## Architecture

The application is split into two deployable applications:

- `backend`: Java 21, Spring Boot 3, Maven, Spring Security, JPA, MySQL, WebSocket, Stripe, Cloudinary, Email.
- `frontend`: React, React Router, Axios, Material UI, Context API, Chart.js, Socket.io client, responsive UI.

Data flow:

1. React calls Spring Boot REST APIs through Axios.
2. Spring Security validates JWT access tokens.
3. Spring Data JPA persists normalized data in MySQL.
4. Businesses create listings with Cloudinary image URLs.
5. Nearby consumers and NGOs receive real-time listing events through WebSocket.
6. Discount orders use Stripe Sandbox; donation claims skip payment.

## Folder Structure

```text
Project/
  backend/
    pom.xml
    src/main/java/com/surplusfood/marketplace/
      config/
      controller/
      dto/
      entity/
      exception/
      mapper/
      repository/
      security/
      service/
      util/
      websocket/
      SurplusFoodMarketplaceApplication.java
    src/main/resources/application.yml
    src/test/java/com/surplusfood/marketplace/
  frontend/
    package.json
    index.html
    src/
      assets/
      components/
      contexts/
      hooks/
      layouts/
      pages/
      services/
      utils/
      App.jsx
      main.jsx
  database/
    schema.sql
  docs/
    STEP_1_PROJECT_SETUP.md
```

## Dependencies

Backend dependencies are in `backend/pom.xml`.

Key backend libraries:

- Spring Boot Web
- Spring Security
- Spring Data JPA
- Hibernate
- Validation
- WebSocket
- MySQL Connector
- JJWT
- Stripe Java SDK
- Cloudinary SDK
- Bucket4j rate limiting
- Spring Mail
- Lombok

Frontend dependencies are in `frontend/package.json`.

Key frontend libraries:

- React
- React Router
- Axios
- Material UI
- Chart.js and React Chart.js
- Socket.io Client

## Database

Run `database/schema.sql` in MySQL. It creates:

- users
- roles
- user_roles
- businesses
- ngo_profiles
- categories
- food_listings
- food_listing_images
- orders
- payments
- donations
- pickup_schedules
- reviews
- wishlist
- notifications
- transactions
- complaints

The `food_listings.version` column is reserved for optimistic locking so we can safely prevent double-purchase race conditions in a later step.

## Commands

Backend:

```powershell
cd backend
mvn clean install
mvn spring-boot:run
```

Frontend:

```powershell
cd frontend
npm install
npm run dev
```

Database:

```powershell
mysql -u root -p < database/schema.sql
```

## Eclipse Setup

1. Open Eclipse.
2. Select this workspace: `C:\Users\kshit\eclipse-workspace`.
3. Import backend with `File > Import > Maven > Existing Maven Projects`.
4. Select `C:\Users\kshit\eclipse-workspace\Project\backend`.
5. Use Java 21.
6. Run `SurplusFoodMarketplaceApplication`.

## API Testing

After the backend starts, the base URL is:

```text
http://localhost:8080/api
```

Step 1 only prepares the project foundation. Authentication APIs will be implemented in Step 2.

## Frontend Integration

The frontend reads the backend URL from:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

Copy `frontend/.env.example` to `frontend/.env` before running the UI.

## Git Commit

Suggested commit for this step:

```powershell
git add .
git commit -m "Initialize project architecture and setup"
```
