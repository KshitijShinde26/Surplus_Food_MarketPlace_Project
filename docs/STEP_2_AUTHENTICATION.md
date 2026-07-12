# Step 2: Authentication and Role-Based Access

## What Was Added

Backend:

- User, Role, and RefreshToken entities.
- User, Role, and RefreshToken repositories.
- Register, login, refresh-token, logout, health, and current-user APIs.
- BCrypt password hashing.
- JWT access tokens with role claims.
- Opaque refresh tokens stored in MySQL.
- Spring Security stateless configuration.
- Role-based admin route protection.
- CORS for the React frontend.
- Global exception handling and validation error responses.

Frontend:

- Axios API client.
- Auth service for register, login, refresh, logout, and current user.
- Auth Context provider.

Database:

- `refresh_tokens` table.

## Folder Structure

```text
backend/src/main/java/com/surplusfood/marketplace/
  config/
    RoleDataInitializer.java
    SecurityConfig.java
  controller/
    AuthController.java
  dto/
    ApiErrorResponse.java
    AuthResponse.java
    FieldErrorResponse.java
    LoginRequest.java
    LogoutRequest.java
    MessageResponse.java
    RefreshTokenRequest.java
    RegisterRequest.java
    UserResponse.java
  entity/
    AccountStatus.java
    RefreshToken.java
    Role.java
    RoleName.java
    User.java
  exception/
    ApiException.java
    ConflictException.java
    GlobalExceptionHandler.java
    ResourceNotFoundException.java
  mapper/
    UserMapper.java
  repository/
    RefreshTokenRepository.java
    RoleRepository.java
    UserRepository.java
  security/
    CustomUserDetailsService.java
    JwtAuthenticationFilter.java
    JwtService.java
    RestAccessDeniedHandler.java
    RestAuthenticationEntryPoint.java
    UserPrincipal.java
  service/
    AuthService.java
    RefreshTokenService.java

frontend/src/
  contexts/
    AuthContext.jsx
  services/
    apiClient.js
    authService.js
```

## Dependencies

Already present in `backend/pom.xml`:

- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa`
- `jjwt-api`
- `jjwt-impl`
- `jjwt-jackson`
- `mysql-connector-j`

Already present in `frontend/package.json`:

- `axios`
- `react-router`
- `@mui/material`

## Database Setup

Run the schema again if this is a fresh database:

```powershell
mysql -u root -p < database\schema.sql
```

If you already created the Step 1 database, add only this table:

```sql
USE surplus_food_marketplace;

CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_refresh_token_user (user_id),
    INDEX idx_refresh_token_expires_at (expires_at)
);
```

## Commands To Run

Backend:

```powershell
cd C:\Users\kshit\eclipse-workspace\Project\backend
mvn clean install
mvn spring-boot:run
```

Frontend:

```powershell
cd C:\Users\kshit\eclipse-workspace\Project\frontend
npm install
npm run dev
```

## API Testing

Base URL:

```text
http://localhost:8080/api
```

Health:

```http
GET /api/auth/health
```

Register consumer:

```http
POST /api/auth/register
Content-Type: application/json

{
  "fullName": "Aarav Mehta",
  "email": "aarav@example.com",
  "phone": "+919999999999",
  "password": "Password123",
  "role": "ROLE_CONSUMER",
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

Login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "aarav@example.com",
  "password": "Password123"
}
```

Current user:

```http
GET /api/auth/me
Authorization: Bearer YOUR_ACCESS_TOKEN
```

Refresh token:

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "YOUR_REFRESH_TOKEN"
}
```

Logout:

```http
POST /api/auth/logout
Content-Type: application/json
Authorization: Bearer YOUR_ACCESS_TOKEN

{
  "refreshToken": "YOUR_REFRESH_TOKEN"
}
```

## Frontend Integration

Auth calls are centralized in:

```text
frontend/src/services/authService.js
```

React auth state is centralized in:

```text
frontend/src/contexts/AuthContext.jsx
```

The API base URL comes from:

```text
frontend/.env
```

Use:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

## Testing Instructions

1. Start MySQL.
2. Run the database schema.
3. Start the backend.
4. Call `/api/auth/health`.
5. Register a user.
6. Login with the same user.
7. Copy the returned access token.
8. Call `/api/auth/me` with `Authorization: Bearer YOUR_ACCESS_TOKEN`.
9. Call `/api/auth/refresh` with the returned refresh token.
10. Call `/api/auth/logout`.

Expected behavior:

- Password is stored as BCrypt, not plaintext.
- Access token expires based on `app.jwt.access-token-expiration-minutes`.
- Refresh token is stored in `refresh_tokens`.
- Business and NGO users register with `PENDING_VERIFICATION`.
- Consumer users register with `ACTIVE`.
- `ROLE_ADMIN` cannot self-register.

## Git Commit

Suggested commit:

```powershell
git add .
git commit -m "Implement JWT authentication and role security"
```
