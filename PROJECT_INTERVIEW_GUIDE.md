# Project Interview Master Class Guide: Surplus Food Marketplace

Welcome to the **Surplus Food Marketplace Interview Master Class Guide**. This document is designed to equip you with the deep technical knowledge, architectural understanding, and confidence required to explain every part of this project in high-stakes software engineering interviews (for roles ranging from Junior to Senior Full-Stack Engineer, Technical Lead, and Software Architect).

Rather than just listing what files do, this guide details **why** choices were made, **how** frameworks (Spring Boot, Hibernate, React) manage resources under the hood, the **exact execution flow** of data, and **strategic interview assets** (Top 100 Q&As, Top 50 Cross-Questions, HR pitches, and Scalability designs).

---

## Table of Contents
1. [Folder Structure & Architectural Blueprint](#1-folder-structure--architectural-blueprint)
2. [Database Design & ER Schema](#2-database-design--er-schema)
3. [Core File Deep Dives & Walkthroughs](#3-core-file-deep-dives--walkthroughs)
4. [Technical Deep Dive: Spring Boot & Hibernate Internals](#4-technical-deep-dive-spring-boot--hibernate-internals)
5. [Security & Authentication Architecture](#5-security--authentication-architecture)
6. [Real-Time Systems & WebSockets](#6-real-time-systems--websockets)
7. [Core Integrations: Stripe & Cloudinary](#7-core-integrations-stripe--cloudinary)
8. [Interview Preparation Assets](#8-interview-preparation-assets)
   - [Top 100 Project Interview Questions](#top-100-project-interview-questions)
   - [Top 50 Interviewer Cross-Questions](#top-50-interviewer-cross-questions)
   - [File-by-File Interview Q&As (Core Components)](#file-by-file-interview-qas-core-components)
   - [Common Mistakes Students Make](#common-mistakes-students-make)
   - [Production Scaling to 1 Million Users](#production-scaling-to-1-million-users)
9. [Interview Pitch & Academic Viva Guide](#9-interview-pitch--academic-viva-guide)

---

## 1. Folder Structure & Architectural Blueprint

### Folder Structure Overview

Below is the directory structure of the project. Understanding where each file lives is essential for demonstrating clean code practices and separation of concerns:

```text
Project/
├── backend/                             # Spring Boot Backend Module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/surplusfood/marketplace/
│   │   │   │   ├── SurplusFoodMarketplaceApplication.java # Spring Boot Entrypoint
│   │   │   │   ├── config/              # Server Configurations (Security, WS, Data Init)
│   │   │   │   │   ├── NotificationScheduler.java
│   │   │   │   │   ├── RoleDataInitializer.java
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   └── WebSocketConfig.java
│   │   │   │   ├── controller/          # REST Controllers (API Endpoints)
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── FoodListingController.java
│   │   │   │   │   ├── PaymentController.java
│   │   │   │   │   └── WebSocketConfig.java (Config folder)
│   │   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   │   ├── AuthResponse.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   └── FoodListingRequest.java
│   │   │   │   ├── entity/              # JPA Database Entities
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Role.java
│   │   │   │   │   ├── FoodListing.java
│   │   │   │   │   └── Transaction.java
│   │   │   │   ├── event/               # Spring Application Events
│   │   │   │   │   ├── FoodListingCreatedEvent.java
│   │   │   │   │   └── NotificationCreatedEvent.java
│   │   │   │   ├── exception/           # Global Exception Handling
│   │   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │   ├── listener/            # Transactional Event Listeners
│   │   │   │   │   └── TransactionEventListener.java
│   │   │   │   ├── mapper/              # Object Mappers (Entity <-> DTO)
│   │   │   │   │   └── FoodListingMapper.java
│   │   │   │   ├── repository/          # Spring Data JPA Repositories
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   └── FoodListingRepository.java
│   │   │   │   └── service/             # Business Logic Layer
│   │   │   │       ├── AuthService.java
│   │   │   │       ├── FoodListingService.java
│   │   │   │       └── StripePaymentService.java
│   │   │   └── resources/
│   │   │       ├── application.yml      # Configuration properties
│   │   │       └── schema.sql
│   │   └── test/                        # Unit and Integration Tests
│   └── pom.xml                          # Maven Dependency Configuration
└── frontend/                            # React Frontend Module (Vite)
    ├── src/
    │   ├── main.jsx                     # Vite Application Entry Point
    │   ├── App.jsx                      # Main Router & Component Tree
    │   ├── components/                  # Reusable UI Components
    │   │   ├── ChatbotWidget.jsx
    │   │   └── Navbar.jsx
    │   ├── contexts/                    # React Contexts for Global State
    │   │   └── AuthContext.jsx          # User Authentication State
    │   ├── pages/                       # Page-level Components
    │   │   ├── Dashboard.jsx
    │   │   └── Login.jsx
    │   ├── services/                    # API Integration clients
    │   │   ├── apiClient.js             # Base Axios Interceptors
    │   │   └── authService.js
    │   └── utils/
    │       └── stompClient.js           # Hand-written STOMP WebSocket Client
    └── package.json                     # NPM dependencies
```

---

### Request-Response Lifecycle & Thread Model

When a user interacts with the React frontend (e.g., submitting a login form or purchasing surplus food), data flows through a strict, multi-tiered architecture. Understanding the mechanics of this lifecycle demonstrates your architectural maturity:

```
[ React UI (Dashboard/Login) ]
             │
             ▼  1. Axios request with Bearer JWT (apiClient.js)
     [ HTTP Network Request ]
             │
             ▼  2. Tomcat Web Server Thread Pool (Acceptor / Worker Thread)
   [ Tomcat Worker Thread ]
             │
             ▼  3. Servlet Filter Chain (OncePerRequestFilter -> SecurityFilterChain)
[ JwtAuthenticationFilter ] (Parses token, validates with JwtService, updates SecurityContext)
             │
             ▼  4. DispatcherServlet (Spring Front Controller maps path to Handler)
 [ Controller (AuthController) ] (Checks validation rules @Valid, maps DTO to Service)
             │
             ▼  5. Dynamic Proxy Transaction Interceptor (AOP proxy opens JPA Transaction)
    [ Service (AuthService) ] (Executes business logic, encodes passwords, applies business rules)
             │
             ▼  6. Spring Data JPA / Hibernate (Generates SQL query, checks L1 Session cache)
[ Repository (UserRepository) ]
             │
             ▼  7. JDBC Connection Pool (HikariCP hands connection to Hibernate)
      [ MySQL Database ] (Executes query, applies locking, returns record)
             │
             ▼  8. JSON Serialization & Commit (Spring writes HTTP Response via Jackson, Closes TX)
   [ React Axios Callback ] (State updates, React re-renders UI)
```

#### Under the Hood: Threading & Request Lifecycle
1. **Tomcat Thread Pool**: When Spring Boot runs, it uses an embedded Apache Tomcat servlet container. Tomcat maintains a thread pool (default size: `200` worker threads). An incoming HTTP request is received by a connector socket, parsed, and assigned to a specific **Tomcat Worker Thread**. This thread is dedicated to handling that request from start to finish (Thread-Per-Request model).
2. **Filter Chain & Spring Security**: The request passes through servlet filters. Spring Security uses a `DelegatingFilterProxy` to route the request through a chain of security filters. One of these is our custom [JwtAuthenticationFilter](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/security/JwtAuthenticationFilter.java), which intercepts the request, reads the `Authorization` header, extracts the JWT, decodes it, and populates Spring's thread-local `SecurityContextHolder`.
3. **DispatcherServlet Dispatching**: The front controller `DispatcherServlet` inspects the request URI and routes it to the matching `@RestController` handler method (e.g., `login()` in [AuthController](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/controller/AuthController.java)).
4. **Transaction AOP interceptor**: If the service method is annotated with `@Transactional`, Spring's AOP (Aspect-Oriented Programming) framework intercepts the call. It obtains a JDBC connection from **HikariCP** and sets auto-commit to `false`, establishing the transaction boundary.
5. **Persistence L1 Cache & Flush**: Hibernate manages entities in the `Persistence Context` (L1 Cache). When repositories are queried or save operations are executed, Hibernate compiles SQL queries. These queries are executed against MySQL. When the service method completes successfully, the transaction interceptor commits the transaction, and the database changes are finalized. If a `RuntimeException` is thrown, the transaction is rolled back.
6. **JSON Serialization**: The controller returns a Java object (like `AuthResponse`). The `HttpMessageConverter` (configured with Jackson under the hood) serializes the object to JSON format and writes it directly to the HTTP response body.

---

## 2. Database Design & ER Schema

The database is structured to model a marketplace where local businesses can post surplus food listings (either for sale at a discount, or for donation), and consumers or NGOs can claim/purchase them.

### Database ER Diagram (Conceptual)

```
        ┌──────────────┐
        │     ROLES    │
        └──────┬───────┘
               │ 1
               │ Many-to-Many
               │ (user_roles)
               │ Many
        ┌──────┴───────┐
        │     USERS    │◄──────────────────────────┐
        └──────┬───────┘                           │ 1
               │ 1                                 │
         ┌─────┴──────────────┐                    │ Many-to-One
         │                    │                    │
       1 │ 1-to-1       1-to-1│ 1                  │
  ┌──────▼──────┐      ┌──────▼──────┐      ┌──────┴──────┐
  │  BUSINESS   │      │ NGO_PROFILE │      │  WISHLIST   │
  └──────┬──────┘      └─────────────┘      └──────┬──────┘
         │ 1                                       │ Many-to-One
         │                                         │
         │ One-to-Many                             │
         │                                         │ 1
         │ Many                                    │
  ┌──────▼──────┐◄─────────────────────────────────┘
  │ FOOD_LISTING│◄─────────────────────────┐
  └──────┬──────┘                          │ 1
         │ 1                               │
         │                                 │ One-to-Many
         │ One-to-Many                     │
         ├──────────────────────┐          │ Many
         │ Many                 │ Many     │
  ┌──────▼──────┐        ┌──────▼──────┐   │
  │ ORDER       │        │  DONATION   │   │
  └──────┬──────┘        └──────┬──────┘   │
         │ 1                    │ 1        │
         │                      │          │
         │ 1-to-1               │ 1-to-1   │
  ┌──────▼──────┐        ┌──────▼──────┐   │
  │   PAYMENT   │        │ TRANSACTION │   │
  └─────────────┘        └─────────────┘   │
                                           │
  ┌────────────────────────────────────────┘
  │ 1 (One-to-Many)
  │
  │ Many
┌─▼──────────────────┐
│ FOOD_LISTING_IMAGE │
└────────────────────┘
```

---

### MySQL Table Generation Schemas

The following physical SQL queries represent the database table generation:

```sql
-- 1. Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    phone VARCHAR(30),
    password_hash VARCHAR(255) NOT NULL,
    account_status VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
);

-- 2. Roles Table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(40) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 3. User Roles Join Table (Many-to-Many)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 4. Business Table (1-to-1 with Users)
CREATE TABLE businesses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    description TEXT,
    business_type VARCHAR(50) NOT NULL,
    verification_document_url VARCHAR(255),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. NGO Profile Table (1-to-1 with Users)
CREATE TABLE ngo_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL UNIQUE,
    ngo_name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(100) NOT NULL UNIQUE,
    address TEXT NOT NULL,
    verification_document_url VARCHAR(255),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 6. Categories Table
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 7. Food Listings Table (Many-to-One with Business, Many-to-One with Category)
CREATE TABLE food_listings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    quantity INT NOT NULL,
    available_quantity INT NOT NULL,
    original_price DECIMAL(10, 2) NOT NULL,
    discount_price DECIMAL(10, 2) NOT NULL,
    listing_type VARCHAR(50) NOT NULL, -- 'SALE' or 'DONATION'
    is_vegetarian BOOLEAN NOT NULL DEFAULT FALSE,
    is_vegan BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'CLAIMED', 'EXPIRED'
    expiry_time TIMESTAMP NOT NULL,
    pickup_start_time TIMESTAMP NOT NULL,
    pickup_end_time TIMESTAMP NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_listing_geo (latitude, longitude)
);

-- 8. Food Listing Images Table (One-to-Many with FoodListing)
CREATE TABLE food_listing_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    cloudinary_public_id VARCHAR(150),
    sort_order INT DEFAULT 0,
    FOREIGN KEY (listing_id) REFERENCES food_listings(id) ON DELETE CASCADE
);

-- 9. Orders Table (Many-to-One with Users, Many-to-One with FoodListing)
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consumer_id BIGINT NOT NULL,
    listing_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_PAYMENT', -- 'PAID', 'COMPLETED', 'FAILED'
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (consumer_id) REFERENCES users(id),
    FOREIGN KEY (listing_id) REFERENCES food_listings(id)
);

-- 10. Payments Table (1-to-1 with Order)
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    stripe_payment_intent_id VARCHAR(150) NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'usd',
    status VARCHAR(50) NOT NULL, -- 'SUCCEEDED', 'FAILED', 'PROCESSING'
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 11. Wishlists Table (Composite Primary Key of user_id and business_id)
CREATE TABLE wishlists (
    user_id BIGINT NOT NULL,
    business_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, business_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE
);
```

---

## 3. Core File Deep Dives & Walkthroughs

This section walks through the core files of the application. For each file, we cover its architectural purpose, code-level annotations, underlying JVM and Spring mechanics, execution flow, and mapping rules.

---

### File 1: [User.java](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/User.java) (JPA Entity)

#### 1. Purpose & Problem Solved
The `User` class is a persistence model representing a system user (which can be a Business Owner, Consumer, NGO Representative, or Administrator). It binds the JVM object state directly to the relational database row in the `users` table. If this file is removed, Spring Data JPA cannot perform Object-Relational Mapping (ORM) for users, breaking authentication, profiles, and transactional references.

#### 2. Responsibilities & SOLID Principles
- **Responsibility**: Storing user identification, hashed security credentials, geographical coordinates for distance-based calculations, and authorization roles.
- **SOLID Principle**: Adheres to the **Single Responsibility Principle (SRP)** as it only holds the data model definition and database mappings, delegating authentication logic to `AuthService` and security logic to `UserPrincipal`.

#### 3. Complete Code Walkthrough
- **`@Getter` / `@Setter` / `@NoArgsConstructor` (Lombok)**: At build time, Lombok processes these annotations and injects standard getter methods, setter methods, and a default public zero-argument constructor into the compiled bytecode. Hibernate *requires* a no-arg constructor because it uses reflection to instantiate the entity when reading rows from the database.
- **`@Entity`**: Marks the class as a JPA entity. During startup, Hibernate scans classpath classes marked with `@Entity` to construct its metadata model (SessionFactory / Metamodel).
- **`@Table(name = "users")`**: Maps the class to the database table named `users` instead of defaulting to the class name.
- **`@Id` & `@GeneratedValue(strategy = GenerationType.IDENTITY)`**: `@Id` marks the field as the primary key. `IDENTITY` relies on the database's auto-increment feature. When saving, Hibernate executes the SQL insert statement first, allowing the database to assign the ID, and then retrieves that ID using `JDBC Statement.getGeneratedKeys()`.
- **`@Column(name = "password_hash", nullable = false)`**: Maps the field `passwordHash` to `password_hash` column. Specifying `nullable = false` tells Hibernate to apply an database-level `NOT NULL` constraint when generating the schema, and checks for null values during save validation.
- **`@ManyToMany(fetch = FetchType.EAGER)`**: Models a many-to-many relationship with `Role`. `FetchType.EAGER` ensures that roles are loaded immediately whenever a User is fetched. This prevents lazy loading exceptions during authentication, where roles are accessed outside the active transaction context.
- **`@JoinTable(...)`**: Defines the intermediate join table `user_roles` linking user IDs to role IDs.

#### 4. Execution Flow
```
Registration Request -> AuthController -> AuthService -> UserRepository.save(User) -> Hibernate (L1 cache checks) -> SQL INSERT -> Database
```

---

### File 2: [SecurityConfig.java](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/SecurityConfig.java) (Spring Security Configuration)

#### 1. Purpose & Problem Solved
This class configures Spring Security's filter chains. It secures endpoints, disables CSRF (since we use stateless JWTs), sets up CORS rules for cross-origin React calls, and plugs our custom JWT authentication filter into the pipeline. If removed, the application becomes insecure, allowing anonymous users to modify marketplace resources.

#### 2. Responsibilities & SOLID Principles
- **Responsibility**: Centralized security settings, declaring encryption beans (BCrypt), and defining path-based authorization permissions.
- **SOLID Principle**: Implements the **Open-Closed Principle (OCP)**; new routes can be secured by modifying configurations here without editing the controllers themselves.

#### 3. Complete Code Walkthrough
- **`@Configuration`**: Tells Spring Boot that this class contains `@Bean` definition methods. The Spring container processes this class and loads the beans into the application context.
- **`@EnableMethodSecurity`**: Enables annotations like `@PreAuthorize` or `@Secured` on controller methods, allowing role-based access checks directly on method signatures.
- **`@Bean public SecurityFilterChain securityFilterChain(...)`**: Configures the filter chain registry. It overrides the default Spring Security filter stack.
- **`.csrf(AbstractHttpConfigurer::disable)`**: Disables CSRF (Cross-Site Request Forgery). CSRF uses session cookies for validation, which is vulnerable. Since JWTs are passed via the HTTP Header `Authorization: Bearer <token>`, the browser does not automatically send them, mitigating CSRF risks.
- **`.sessionManagement(...)` with `SessionCreationPolicy.STATELESS`**: Instructs Spring Security not to create or use a `HttpSession`. Security context is established per request via the JWT token.
- **`UsernamePasswordAuthenticationFilter.class`**: Our `jwtAuthenticationFilter` is added *before* this filter to ensure authentication context is populated before standard form authentication takes place.

#### 4. Execution Flow
```
Incoming Request -> DelegatingFilterProxy -> SecurityFilterChain -> JwtAuthenticationFilter -> Controller
```

---

### File 3: [JwtAuthenticationFilter.java](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/security/JwtAuthenticationFilter.java) (Servlet Filter)

#### 1. Purpose & Problem Solved
This filter intercepts every incoming HTTP request exactly once. It extracts the JWT token from the `Authorization` header, validates it, and puts the authenticated user principal into Spring's thread-local `SecurityContextHolder`. If this file is removed, incoming requests will not be authenticated, and protected endpoints will return HTTP 401/403 errors.

#### 2. Responsibilities & SOLID Principles
- **Responsibility**: Header parsing, JWT validation delegating, and Security Context updates.
- **SOLID Principle**: Follows **Single Responsibility Principle (SRP)** by keeping authentication filter logic isolated from JWT generation logic.

#### 3. Complete Code Walkthrough
- **`extends OncePerRequestFilter`**: A Spring-specific helper class that guarantees the filter is executed exactly once per servlet request cycle, preventing redundant intercepts.
- **`doFilterInternal`**: The core interceptor method.
  - Extracts the bearer token using `resolveToken(request)`.
  - Checks if a token exists and that the `SecurityContextHolder` is not already populated (which prevents redundant lookups if another filter has authenticated the request).
  - Calls `jwtService.extractUsername(token)` to decode the token.
  - Loads user details using `userDetailsService.loadUserByUsername(username)`.
  - Checks token expiration and status (account non-locked, etc.).
  - Creates a `UsernamePasswordAuthenticationToken` containing the user principal, credentials (null), and authorities.
  - Sets the authentication token in the context using `SecurityContextHolder.getContext().setAuthentication(...)`.
  - Finally, calls `filterChain.doFilter(request, response)` to pass control to the next filter in the stack.

---

### File 4: [AuthService.java](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/AuthService.java) (Business Logic Service)

#### 1. Purpose & Problem Solved
Contains transactions and business rules for user onboarding, token-based authentication (sign-in), token refresh, and user sign-out. It prevents direct access to repositories and ensures business rules are enforced during registration (e.g., preventing self-registration of Admin accounts, email normalization, hashing passwords).

#### 2. Responsibilities & SOLID Principles
- **Responsibility**: Authenticating credentials, registering accounts, generating token pairs (Access + Refresh tokens).
- **SOLID Principle**: Adheres to the **Single Responsibility Principle (SRP)** by encapsulating authentication workflows.

#### 3. Complete Code Walkthrough
- **`@Service`**: A specialization of `@Component` that marks this class as a business service bean, making it eligible for dependency injection.
- **`@Transactional`**: Instructs Spring's transaction manager to open a new database transaction. If the execution completes normally, the transaction commits. If a `RuntimeException` or `Error` occurs, the transaction is rolled back, protecting database integrity.
- **`register(...)` Method**:
  - Checks if the requested registration role is ADMIN; if so, throws an `ApiException` with HTTP 400.
  - Normalizes the email string (`trim().toLowerCase()`) to prevent duplicate email entries.
  - Calls `userRepository.existsByEmailIgnoreCase(...)` to ensure email uniqueness.
  - Encodes the plain-text password using the `PasswordEncoder` implementation bean (which is BCrypt with strength 12, configured in `SecurityConfig`).
  - Sets user properties and roles.
  - Saves the user entity using `userRepository.save(user)`.
  - Returns `createAuthResponse(user)`.
- **`login(...)` Method**:
  - Normalizes the email.
  - Invokes `authenticationManager.authenticate(...)` with `UsernamePasswordAuthenticationToken`. This delegate calls the `DaoAuthenticationProvider`, which fetches details via `CustomUserDetailsService` and checks passwords using BCrypt.
  - If credentials are valid, it generates access and refresh tokens.

---

### File 5: [AuthController.java](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/controller/AuthController.java) (REST Controller)

#### 1. Purpose & Problem Solved
Exposes RESTful endpoints `/api/auth/*` to client applications. It receives JSON request payloads, validates input fields, and forwards the arguments to the `AuthService` layer. If removed, the React client cannot connect to the backend authentication services.

#### 2. Responsibilities & SOLID Principles
- **Responsibility**: Mapping HTTP POST/GET requests to service methods, returning serializable response payloads, and enforcing constraint checks.
- **SOLID Principle**: Implements the **Single Responsibility Principle (SRP)** by managing HTTP routing and parameter parsing, leaving business logic to services.

#### 3. Complete Code Walkthrough
- **`@RestController`**: Combines `@Controller` and `@ResponseBody`. Tells Spring Boot that this class is an API controller, and that every method's return value will be written directly to the HTTP response body as JSON.
- **`@RequestMapping("/auth")`**: Prepends `/auth` to all method paths inside this controller.
- **`@Valid`**: Instructs Spring's validation engine (Hibernate Validator) to validate properties inside the incoming request DTO (e.g., checking if email is valid format, passwords are not blank). If constraints fail, it throws a `MethodArgumentNotValidException`, which is caught by our `GlobalExceptionHandler`.
- **`@RequestBody`**: Maps the incoming HTTP request JSON body into the target Java object using Jackson's object mapper.
- **`@AuthenticationPrincipal UserPrincipal principal`**: Resolves the current authenticated user details from the `SecurityContextHolder`.

---

### File 6: [FoodListing.java](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/FoodListing.java) (JPA Entity)

#### 1. Purpose & Problem Solved
Represents a surplus food offering posted by a business. It maps attributes like quantity, discount price, expiry dates, coordinates, vegetarian indicator, and type (sale vs donation). If removed, the application has no database mapping for listings, rendering the marketplace inoperable.

#### 2. Responsibilities & SOLID Principles
- **Responsibility**: Representing listing state and relational mapping schemas for food listings and associated images.
- **SOLID Principle**: Single Responsibility Principle (SRP) - holds model state and mappings.

#### 3. Complete Code Walkthrough
- **`@Entity` & `@Table(name = "food_listings")`**: Declares this class as a database-mapped model for `food_listings`.
- **`private Long id`**: The auto-incremented primary key column.
- **`@ManyToOne(fetch = FetchType.LAZY)`**: Maps a listing back to a single `Business`. `FetchType.LAZY` prevents loading the business details into memory unless explicitly requested. This avoids fetching large parent objects when listing food items in bulk.
- **`@OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)`**: Models the association with listing images.
  - `mappedBy = "listing"`: Informs Hibernate that the `FoodListingImage` entity owns the relationship.
  - `cascade = CascadeType.ALL`: Directs Hibernate to cascade parent changes to child images (e.g., deleting a listing deletes its images).
  - `orphanRemoval = true`: Deleting an image from the list automatically executes an SQL `DELETE` query for that image record in the database.

---

### File 7: [FoodListingService.java](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/FoodListingService.java) (Domain Service)

#### 1. Purpose & Problem Solved
Implements business rules for creating, updating, and querying food listings. It validates if the posting business is verified by administrators, calculates listing locations, and publishes events when listings are created. If removed, listings cannot be created or updated safely.

#### 2. Responsibilities & SOLID Principles
- **Responsibility**: Enforcing listing validation, repository updates, and event publishing.
- **SOLID Principle**: Open-Closed Principle (OCP) - handles event publishing via an event bus, allowing other systems to react without modifying the listing service.

#### 3. Complete Code Walkthrough
- **`private final ApplicationEventPublisher eventPublisher`**: Spring's event publication channel.
- **`createListing(...)` Method**:
  - Fetches the `Business` profile. If the business is not verified (`!business.isVerified()`), it throws a `ForbiddenException` (HTTP 403).
  - Instantiates `FoodListing` and maps request parameters (prices, quantities, type).
  - If image URLs are supplied, maps them to child images.
  - Saves the listing to MySQL using `foodListingRepository.save(listing)`.
  - Publishes a `FoodListingCreatedEvent` (line 90) using `eventPublisher.publishEvent(...)`.

---

### File 8: [UserRepository.java](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/repository/UserRepository.java) (Spatial Query Repository)

#### 1. Purpose & Problem Solved
Implements database operations for users. It provides queries to fetch records by email and calculates nearby users within a specific radius using coordinates. If removed, the application cannot run queries to find users near a newly posted listing.

#### 2. Responsibilities & SOLID Principles
- **Responsibility**: Database communication and spatial computations.
- **SOLID Principle**: Single Responsibility Principle (SRP) - repository data access layer.

#### 3. Complete Code Walkthrough
- **`extends JpaRepository<User, Long>`**: Provides access to CRUD operations, pagination, and sorting.
- **`existsByEmailIgnoreCase(String email)`**: Spring translates this method signature into an SQL query: `SELECT count(*) FROM users WHERE LOWER(email) = LOWER(?)`.
- **`@Query(..., nativeQuery = true)`**: Declares a native SQL query that bypasses Hibernate's JPQL parser.
- **Haversine Formula**:
  `6371 * acos(cos(radians(:latitude)) * cos(radians(u.latitude)) * cos(radians(u.longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(u.latitude)))`
  This formula computes the great-circle distance between two points on a sphere (in this case, Earth, using radius 6371 km). It joins users with user roles, checks if the roles are `ROLE_CONSUMER` or `ROLE_NGO`, and filters the results using the `HAVING` clause based on the specified radius.

---

### File 9: [TransactionEventListener.java](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/listener/TransactionEventListener.java) (Event Listener)

#### 1. Purpose & Problem Solved
Listens for system events and handles post-transaction processing. It broadcasts listings over WebSockets, looks up nearby users, and dispatches email alerts asynchronously. If removed, notifications and emails will fail, and real-time updates will not be sent to clients.

#### 2. Responsibilities & SOLID Principles
- **Responsibility**: Asynchronous event dispatching, WebSocket broadcasts, and email delivery.
- **SOLID Principle**: Adheres to the **Single Responsibility Principle (SRP)** by decoupling notification flows from core business services.

#### 3. Complete Code Walkthrough
- **`@Component`**: Registers the listener as a Spring bean.
- **`@Async`**: Runs the annotated method in a background thread pool, freeing the main HTTP request thread.
- **`@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`**:
  Indicates that this listener should only process the event *after* the current database transaction commits. This is a critical pattern: if the database save fails and rolls back, no notifications or emails are sent, preventing inconsistent states.
- **`handleFoodListingCreated(...)` Method**:
  - Broadcasts the new listing payload to the `/topic/listings` WebSocket endpoint using the `SimpMessagingTemplate`.
  - Resolves nearby users using `userRepository.findNearbyConsumersAndNgos(...)`.
  - Dispatches database and email notifications to those users.

---

### File 10: [StripePaymentService.java](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/StripePaymentService.java) (Payment Service)

#### 1. Purpose & Problem Solved
Handles card payments using Stripe. It creates PaymentIntents and processes webhook notifications to confirm when transactions succeed or fail. If removed, users cannot pay for their surplus food orders, preventing monetization.

#### 2. Responsibilities & SOLID Principles
- **Responsibility**: Stripe API communication, PaymentIntent generation, and webhook processing.
- **SOLID Principle**: Implements the **Dependency Inversion Principle (DIP)**. It abstracts Stripe interactions, allowing the application to fall back to a mock mode when Stripe is unconfigured.

#### 3. Complete Code Walkthrough
- **`@PostConstruct`**: Runs immediately after bean instantiation and dependency injection. It initializes Stripe's API key.
- **`createPaymentIntent(...)` Method**:
  - Checks if the order is in `PENDING_PAYMENT` status.
  - Multiplies the order amount by 100 to convert dollars to cents (Stripe's default currency unit).
  - If a Stripe key is configured, it sends a request to Stripe to create a PaymentIntent. Otherwise, it falls back to a sandbox mode, generating mock client secrets.
  - Updates the order's payment record status to `REQUIRES_PAYMENT_METHOD` and persists it.
- **`processWebhookEvent(...)` Method**:
  - Parses the incoming webhook JSON body.
  - Verifies the signature to confirm the payload came from Stripe.
  - Checks the event type. If it is `payment_intent.succeeded`, it updates the payment status to `SUCCEEDED`, changes the order status to `PAID`, schedules the pickup, logs the transaction, and dispatches confirmation notifications.

---

### File 11: [SimpleStompClient.js](file:///c:/Users/kshit/eclipse-workspace/Project/frontend/src/utils/stompClient.js) (React WebSocket Client)

#### 1. Purpose & Problem Solved
A custom React client that handles real-time communications. It establishes connection upgrades, parses incoming STOMP frames, handles subscription routing, and automatically processes subscription queues. It is written in pure JavaScript, removing the need for external libraries like `@stomp/stompjs`.

#### 2. Responsibilities & SOLID Principles
- **Responsibility**: Maintaining socket connections, parsing frames, and handling subscriptions.
- **SOLID Principle**: Follows the **Single Responsibility Principle (SRP)** by keeping real-time communication concerns separate from the React UI components.

#### 3. Complete Code Walkthrough
- **`connect()` Method**:
  - Converts standard HTTP/HTTPS URLs to WS/WSS endpoints.
  - Instantiates a standard browser `WebSocket` connection.
  - **`ws.onopen`**: Sends a STOMP connection frame:
    `CONNECT\naccept-version:1.1,1.2\nheart-beat:10000,10000\n\n\u0000`
  - **`ws.onmessage`**: Handles incoming messages. If the frame starts with `CONNECTED`, it updates its connection status and registers pending subscriptions. If the frame starts with `MESSAGE`, it extracts the body, parses the JSON payload, and executes the registered callbacks.
- **`subscribe(destination)` Method**:
  - Registers subscription paths (e.g., `/topic/listings` or `/user/queue/notifications`).
  - Calls `sendSubscribeFrame(...)` to register the path with the Spring Boot broker.

---

## 4. Technical Deep Dive: Spring Boot & Hibernate Internals

Understanding what happens behind the scenes of Spring Boot and Hibernate is a key differentiator in advanced interviews.

### Spring Boot Lifecycle & Dependency Injection

#### Application Startup Flow
1. **Bootstrap**: When you run `SpringApplication.run(Application.class, args)`, the Spring container initializes. It determines the application type (Servlet web environment using Tomcat) and sets up a `ConfigurableApplicationContext`.
2. **Component Scanning**: Spring scans the project classpath starting from the package where the main class is declared (e.g., `com.surplusfood.marketplace`). It identifies classes marked with stereotypic annotations: `@Component`, `@Service`, `@Repository`, `@RestController`, and `@Configuration`.
3. **Bean Definition Creation**: For each candidate class, Spring creates a `BeanDefinition` object describing the bean's class, scope (singleton, prototype), and autowire requirements.
4. **Bean Instantiation**: Spring instantiates beans. The container resolves constructor arguments and uses reflection to instantiate each bean.
5. **Dependency Injection & Autowiring**:
   - **Why Constructor Injection is Preferred**: Using constructor injection (via Lombok's `@RequiredArgsConstructor` annotation) ensures that all required dependencies are provided at creation time. This guarantees that beans are immutable, makes writing unit tests easier because dependencies can be passed manually, and detects circular dependencies during startup instead of at runtime.
   - **How Proxies are Created**: If a class uses annotations like `@Transactional` or `@Async`, Spring does not register the raw bean directly in the Application Context. Instead, it creates a proxy wrapper using **CGLIB** (which subclasses the target class) or **JDK Dynamic Proxies** (if the class implements an interface). When other beans reference this dependency, they interact with the proxy wrapper.

---

### Hibernate & JPA Entity Lifecycles

An entity instance transition phases under JPA. Interviewers frequently ask about these transitions:

```
           [ New Entity (Instantiation) ] (Transient State: Not saved in database, no ID)
                         │
                         ▼  save() / persist()
          [ Managed Entity (Session Context) ] (Managed State: Saved, has ID, tracked for dirty checks)
             ┌───────────┴───────────┐
             │                       │
             ▼  close() / detach()   ▼  remove() / delete()
     [ Detached State ]     [ Removed State ] (Pending DB delete on flush)
             │
             ▼  merge()
      [ Managed State ]
```

- **Transient State**: The object is instantiated in JVM memory (`User user = new User()`) but has no database primary key or association with a persistence context. If garbage collected, its state is lost.
- **Managed State**: The object is associated with a persistence context (session). It has a primary key. Hibernate monitors the entity's fields. If a field value changes, Hibernate flags it as "dirty". When the transaction commits, Hibernate flushes the changes by generating an SQL `UPDATE` statement automatically without you calling `save()`.
- **Detached State**: The object has a primary key but the persistence context that tracked it has closed (or the entity was explicitly detached). Changes made to detached objects are not monitored by Hibernate and will not be saved automatically.
- **Removed State**: The entity is marked for deletion (`entityManager.remove(entity)`). The record will be deleted from the database during the next transaction flush cycle.

---

### Transaction Management Under the Hood

When a method marked with `@Transactional` is called:

```
[ Controller Method Call ]
            │
            ▼
    [ Transaction AOP Proxy ]
            │
            ├─► 1. Opens Connection (HikariCP)
            ├─► 2. Sets AutoCommit to FALSE
            │
            ▼
    [ Target Service Method ] (Executes queries, modifies entities)
            │
            ▼
    [ Transaction AOP Proxy ]
            │
            ├─► 3a. Succeeded? Commit transaction
            └─► 3b. Failed (RuntimeException)? Rollback transaction
            │
            ▼
[ Return Response to Controller ]
```

#### Propagation Behavior: REQUIRED vs. REQUIRES_NEW
- **`REQUIRED`** (Default): Executes within the caller's active transaction. If no transaction is active, it opens a new one. If an exception occurs, the entire transaction is rolled back.
- **`REQUIRES_NEW`**: Suspends the active transaction and opens a new, independent transaction. If the new transaction rolls back, it does not affect the outer suspended transaction. This is useful for writing logs or audit trails that must be saved even if the main operation fails.

#### Rollback Gotchas
By default, Spring rolls back transactions only for **un-checked exceptions** (`RuntimeException` and its subclasses, plus `Error`). If a method throws a **checked exception** (subclasses of `Exception` that are not `RuntimeException`), the transaction will **not** roll back. To change this behavior, you must explicitly declare the rollback rules: `@Transactional(rollbackFor = Exception.class)`.

---

### The N+1 Query Problem & Fetch Joins

The **N+1 Query Problem** occurs when an application loads a collection of parent entities (e.g., fetching 100 food listings) and then loops through them to access a lazily fetched relationship (e.g., referencing each listing's parent `Business`).

#### The Problem:
1. One query fetches the listings:
   `SELECT * FROM food_listings;` (Returns 100 rows).
2. For each listing, the application retrieves the business:
   `SELECT * FROM businesses WHERE id = ?;` (Executed 100 times).
This results in **101 database queries** (1 initial query + N dependent queries), which degrades performance.

#### The Solution: Fetch Joins
Use JPQL `@Query` annotations to specify a **Fetch Join**, directing Hibernate to retrieve the related entities in the initial SQL statement using a SQL `JOIN`:
```java
@Query("SELECT fl FROM FoodListing fl JOIN FETCH fl.business WHERE fl.status = :status")
List<FoodListing> findAllActiveWithBusiness(@Param("status") FoodListingStatus status);
```
This reduces the database operations to **1 single query** containing a SQL `JOIN` clause, fetching all required data in a single round-trip.

---

## 5. Security & Authentication Architecture

Authentication is stateless and managed via secure JWT (JSON Web Tokens).

### The Authentication Flow (Login Process)

```
[ React UI ] ───── (POST /api/auth/login with credentials) ────► [ Tomcat Worker Thread ]
                                                                        │
                                                                        ▼
                                                         [ AuthenticationManager ]
                                                                        │
                                                                        ▼
                                                        [ DaoAuthenticationProvider ]
                                                                        │
                                              ┌─────────────────────────┴────────────────────────┐
                                              ▼                                                  ▼
                                [ CustomUserDetailsService ]                            [ PasswordEncoder ]
                             (Loads User details from Database)                       (Verifies password hash)
                                              │                                                  │
                                              └─────────────────────────┬────────────────────────┘
                                                                        ▼
                                                        [ Successful Authentication? ]
                                                                        │
                                              ┌─────────────────────────┴────────────────────────┐
                                              ▼ (Yes)                                            ▼ (No)
                                     [ AuthService ]                                    [ GlobalExceptionHandler ]
                        (Generates Access and Refresh Tokens)                            (Returns 401 Unauthorized)
                                              │
                                              ▼
                    [ HTTP response JSON (Token + User Response) ]
```

---

### The Token Verification Flow (Subsequent Requests)

For all protected requests, the React client attaches the Access Token in the request header:
`Authorization: Bearer <accessToken>`

```
[ Request Header ] ───► [ JwtAuthenticationFilter ] ───► [ Extracts token value ]
                                                                  │
                                                                  ▼
                                                      [ JwtService.extractUsername ]
                                                                  │
                                                                  ▼
                                                      [ CustomUserDetailsService ]
                                                    (Loads details from Database)
                                                                  │
                                                                  ▼
                                                      [ JwtService.isTokenValid ]
                                                                  │
                                                      ┌───────────┴───────────┐
                                                      ▼ (Valid)               ▼ (Invalid)
                                           [ SecurityContextHolder ]       [ SecurityContext cleared ]
                                           (Sets authenticated User)       (Filter chain routes to EntryPoint)
                                                      │                               │
                                                      ▼                               ▼
                                            [ Routes to Controller ]         [ HTTP 401 response ]
```

---

### Key Security Concepts Explained

#### BCrypt Hashing Algorithm
BCrypt is a adaptive hashing algorithm based on the Blowfish cipher. It incorporates a **salt** (a random value appended to the password before hashing) to protect against rainbow table attacks.
BCrypt uses a **work factor** (cost parameter, set to `12` in our configuration). The cost factor determines the hashing complexity, increasing the time required to compute the hash. This protects the passwords against brute-force attacks by increasing the computing power required to crack them.

#### Refresh Token Rotation (RTR)
Access tokens are short-lived (e.g., 15 minutes) to minimize exposure if compromised. Refresh tokens are long-lived (e.g., 7 days) and stored securely in the database.
When the access token expires, the client sends the refresh token to `/api/auth/refresh` to obtain a new token pair. When this endpoint is called, the refresh token is marked as used and a new refresh token is issued (Refresh Token Rotation). If a reused refresh token is presented, the system immediately revokes all refresh tokens associated with that user to prevent unauthorized access.

---

## 6. Real-Time Systems & WebSockets

### How STOMP WebSockets Work

WebSockets provide full-duplex, real-time communication channels over a single TCP connection.
The system uses the **STOMP** (Simple Text Oriented Messaging Protocol) subprotocol over WebSockets to structure message payloads.

```
React Client                                                               Spring Boot Broker
    │                                                                             │
    ├─────────── 1. HTTP GET Upgrade Request (ws://localhost:8080/ws) ──────────►│
    │                                                                             │
    ◄─────────── 2. HTTP 101 Switching Protocols (TCP established) ──────────────┤
    │                                                                             │
    ├─────────── 3. STOMP CONNECT Frame ─────────────────────────────────────────►│
    │                                                                             │
    ◄─────────── 4. STOMP CONNECTED Frame ────────────────────────────────────────┤
    │                                                                             │
    ├─────────── 5. STOMP SUBSCRIBE (/topic/listings) ───────────────────────────►│
    │                                                                             │
    │                                                                             │
    │            * Business posts new food listing *                              │
    │            * TransactionEventListener handles listing *                      │
    │                                                                             │
    ◄─────────── 6. STOMP MESSAGE Frame (Broadcast JSON) ─────────────────────────┤
```

---

## 7. Core Integrations: Stripe & Cloudinary

### 1. Stripe Payment Gateway integration
The payment flow ensures the marketplace can process card payments securely:
1. **PaymentIntent Creation**: When a consumer places an order, the backend creates a Stripe `PaymentIntent` containing the amount in cents. The Stripe API returns a `client_secret` identifier.
2. **Checkout Processing**: The React client uses Stripe Elements to collect card details and submit payment confirmation directly to Stripe.
3. **Stripe Webhook Verification**: Once processing is complete, Stripe sends an asynchronous HTTP POST webhook containing a `payment_intent.succeeded` event to the backend. The backend verifies the event payload using a signature header, updates the order status to `PAID`, schedules the pickup, and logs the transaction.

---

### 2. Cloudinary Image Upload Flow
Food listings include images uploaded through Cloudinary:
1. **Frontend Upload**: The React application uploads images directly to Cloudinary's secure servers, keeping file upload traffic away from the Spring Boot API.
2. **Metadata Persistence**: Cloudinary returns an image URL and a `public_id`. The client includes these identifiers in the listing creation payload.
3. **Database Records**: The backend stores the URL and identifier in the `food_listing_images` table, linking them to the food listing.

---

## 8. Interview Preparation Assets

This section contains preparation assets to help you prepare for technical interviews.

---

### Top 100 Project Interview Questions

<details>
<summary><b>Category 1: Spring Boot Core & Configuration (Questions 1 - 25)</b></summary>

#### Q1: What is the main purpose of `@SpringBootApplication`?
**Answer**: It is a convenience annotation that combines `@SpringBootConfiguration` (registers configuration classes), `@EnableAutoConfiguration` (enables Spring Boot's autowire features), and `@ComponentScan` (directs Spring to scan the package hierarchy for annotated components).

#### Q2: What is the difference between `@Component`, `@Service`, and `@Repository`?
**Answer**: They are stereotyped annotations. `@Component` is a generic annotation for any Spring-managed bean. `@Service` marks classes containing business logic. `@Repository` registers data access components and translates database exceptions into Spring's unified data access exception hierarchy.

#### Q3: Why did you use Constructor Injection instead of `@Autowired` on fields?
**Answer**: Constructor injection ensures dependencies are required, creates immutable objects, prevents circular dependencies during startup, and simplifies writing unit tests by allowing dependencies to be passed manually.

#### Q4: How does Spring Boot's application context load properties from `application.yml`?
**Answer**: At startup, Spring Boot uses its property sources loaders to parse the YAML structure, loading values into the environment context where they can be accessed using `@Value` or `@ConfigurationProperties`.

#### Q5: What is the default scope of a Spring Bean, and how does it work?
**Answer**: The default scope is **Singleton**. Spring creates exactly one instance of the bean within the application context. This instance is shared and injected across all components requesting that bean.

#### Q6: How does `@PostConstruct` work, and when is it executed?
**Answer**: It is run exactly once on a bean after the Spring container has instantiated the class and injected all dependencies, but before the bean is put into service.

#### Q7: What is the role of `CommandLineRunner` or `ApplicationRunner` in Spring Boot?
**Answer**: Interfaces used to run code blocks immediately after the application context completes startup. We use `RoleDataInitializer` to verify and seed security roles into the database.

#### Q8: How did you configure CORS in your application?
**Answer**: By registering a `CorsConfigurationSource` bean in our security configurations. It defines allowed origins, methods, and headers to support requests from the React client.

#### Q9: What happens behind the scenes when a dependency is marked as optional in Maven?
**Answer**: An optional dependency is excluded from transitive dependency resolution. If project A depends on project B, and B has an optional dependency, Project A must explicitly declare the dependency to use it.

#### Q10: How do you handle environment variables in `application.yml`?
**Answer**: Using placeholder syntax: `${ENV_VAR_NAME:default_value}`. If the environment variable exists, Spring injects its value; otherwise, it falls back to the default value.

#### Q11: What is the difference between `@RestController` and `@Controller`?
**Answer**: `@RestController` combines `@Controller` and `@ResponseBody`. It indicates that the class exposes REST endpoints, and that all method return values should be serialized directly to the HTTP response body as JSON.

#### Q12: How is bean creation managed in Spring Boot?
**Answer**: The Spring container scans classes, builds definitions, instantiates beans using constructors, injects dependencies, calls lifecycle methods (like `@PostConstruct`), and caches the resulting singleton instances in the Application Context.

#### Q13: What is Spring AOP, and how does your project use it?
**Answer**: Aspect-Oriented Programming separates cross-cutting concerns (like transaction management or logging). The project uses AOP via annotations like `@Transactional` and `@Async` to wrap method calls in proxy objects.

#### Q14: How does `@Value` resolve properties in Spring Boot?
**Answer**: It resolves placeholders against the Spring Environment (which aggregates properties from YAML files, system variables, and command-line arguments) and injects the value into the field at startup.

#### Q15: What is the purpose of `@EnableMethodSecurity`?
**Answer**: It activates method-level authorization checks, enabling annotations like `@PreAuthorize("hasRole('ADMIN')")` directly on controller methods.

#### Q16: How do you validate request bodies in Spring Boot?
**Answer**: By adding validation annotations (like `@NotBlank`, `@Size`) to the properties of request DTOs and annotating the controller parameters with `@Valid`.

#### Q17: What exception is thrown if `@Valid` input validation fails?
**Answer**: It throws a `MethodArgumentNotValidException`. Our global exception handler intercepts this exception and extracts field errors to build a clean JSON response.

#### Q18: What is Spring Boot auto-configuration?
**Answer**: Spring Boot analyzes project dependencies (e.g., checking if `mysql-connector` is in the classpath) and automatically registers required configuration beans (like datasource configurations) to simplify setup.

#### Q19: Why do we use `@RequiredArgsConstructor` from Lombok?
**Answer**: It generates a constructor containing all parameters marked as `final`, simplifying constructor injection by eliminating boilerplate code.

#### Q20: How does Spring Boot support multiple configuration profiles?
**Answer**: Using naming patterns like `application-{profile}.yml`. You can activate specific profiles using property settings: `spring.profiles.active=prod`.

#### Q21: What is the role of `HttpMessageConverter` in Spring MVC?
**Answer**: It serializes Java objects returned by controllers into HTTP response formats (like JSON) and deserializes HTTP request bodies into DTO objects.

#### Q22: What is the difference between field injection and constructor injection?
**Answer**: Field injection uses reflection to inject dependencies directly into fields, bypassing constructors. Constructor injection requires dependencies to be declared in constructors, promoting testability and immutability.

#### Q23: How do you customize the embedded Tomcat port?
**Answer**: By setting the `server.port` property in `application.yml` (e.g., `server.port: 8080`).

#### Q24: What is the purpose of the target folder in a Maven project?
**Answer**: It is the default directory where Maven places compiled class files, packaged JAR files, and temporary build assets.

#### Q25: How does Lombok generate getters and setters at compile time?
**Answer**: Lombok runs as an annotation processor during compilation. It modifies the Abstract Syntax Tree (AST) of the Java class, adding the compiled getters, setters, and constructors directly to the bytecode.

</details>

<details>
<summary><b>Category 2: Hibernate, JPA & Database Mapping (Questions 26 - 50)</b></summary>

#### Q26: What is the difference between JPA and Hibernate?
**Answer**: JPA (Jakarta Persistence API) is a specification defining standard object-relational mapping interfaces. Hibernate is an ORM library that implements the JPA specification.

#### Q27: How does Hibernate's First Level Cache (L1 Cache) work?
**Answer**: The L1 cache is associated with the JPA Session. It is active for the duration of a transaction. If an entity is queried multiple times within the same transaction, Hibernate retrieves it from the L1 cache instead of executing redundant database queries.

#### Q28: What is the difference between Lazy and Eager loading?
**Answer**: Lazy loading defers fetching related entities from the database until they are accessed in code. Eager loading retrieves all related entities immediately in the initial query.

#### Q29: What is the N+1 query problem, and how do you resolve it?
**Answer**: The N+1 query problem occurs when an application executes one query to fetch parent entities and then executes N subsequent queries to load lazy relationships for each parent. It is resolved using **Fetch Joins** in JPQL queries.

#### Q30: How does `@GeneratedValue(strategy = GenerationType.IDENTITY)` map to MySQL?
**Answer**: It maps to MySQL's `AUTO_INCREMENT` primary key column type. The database assigns the ID automatically during insertion.

#### Q31: What is the purpose of the `@Version` annotation in JPA?
**Answer**: It enables optimistic locking. Hibernate checks the version number before updates to prevent concurrent transactions from overwriting each other's changes.

#### Q32: What is a Fetch Join, and how does it differ from a standard Join?
**Answer**: A standard join retrieves parent entity fields and checks join criteria. A Fetch Join retrieves both parent and child entities in a single query, populating the target relationships immediately.

#### Q33: How does Hibernate perform dirty checking?
**Answer**: Hibernate maintains a snapshot of each entity when it is loaded. During transaction commits, Hibernate compares the entity's current state with the snapshot. If changes are detected, it generates and executes the required update queries automatically.

#### Q34: What is the difference between `save()` and `saveAndFlush()` in Spring Data JPA?
**Answer**: `save()` caches changes in Hibernate's L1 cache, delaying database updates until the transaction commits. `saveAndFlush()` executes the update queries immediately, sending the changes to the database before the transaction finishes.

#### Q35: What happens when an entity is in the "Detached" state?
**Answer**: The entity exists in JVM memory but is no longer tracked by the Hibernate session. Field changes are not monitored, and updates must be merged back into a session to be saved.

#### Q36: How does the `@JoinColumn` annotation work?
**Answer**: It specifies the foreign key column name in the database table that links the relationship to the parent entity.

#### Q37: How do you map a Many-to-Many relationship in JPA?
**Answer**: Using `@ManyToMany` along with `@JoinTable`. The join table configuration defines the foreign keys pointing to each target table.

#### Q38: What is the purpose of `spring.jpa.hibernate.ddl-auto=update`?
**Answer**: It tells Hibernate to scan JPA entities at startup and update the database schema automatically to match modifications to the entity classes.

#### Q39: What is the danger of using `ddl-auto=create-drop` in production?
**Answer**: It drops all database tables during startup and recreation, leading to immediate data loss.

#### Q40: How does the `@Transactional` annotation open a database transaction?
**Answer**: Spring's AOP framework intercepts the method call, borrows a database connection from HikariCP, disables auto-commit mode, executes the method, and commits or rolls back the changes based on success.

#### Q41: How do spatial coordinates get queried in your UserRepository?
**Answer**: Using native SQL queries implementing the Haversine formula to compute distances between latitude and longitude coordinates.

#### Q42: What is the difference between `JPQL` and native SQL queries?
**Answer**: JPQL queries entity classes and JVM properties. Native SQL queries target physical database tables and column names directly.

#### Q43: How does the JPA entity lifecycle transition from Transient to Managed?
**Answer**: By passing the transient object to `entityManager.persist()` or calling `save()` on a Spring Data JPA repository.

#### Q44: What are the risks of using `@ManyToMany(fetch = FetchType.EAGER)`?
**Answer**: It retrieves all related collections immediately, which can result in large database queries and memory issues if tables contain many records.

#### Q45: How do you resolve a `LazyInitializationException`?
**Answer**: Ensure lazy relationships are accessed within an active transaction, or retrieve them using fetch joins during the initial database query.

#### Q46: What is the default connection pool used by Spring Boot?
**Answer**: **HikariCP**, which is configured by default for its speed and connection handling.

#### Q47: How does `CascadeType.ALL` work?
**Answer**: It propagates all entity lifecycle state changes (like persist, merge, remove) from the parent entity down to its associated child entities.

#### Q48: What is the role of `orphanRemoval = true` in JPA?
**Answer**: If a child entity is removed from a parent's collection, Hibernate deletes the child record from the database automatically.

#### Q49: Why should you avoid exposing raw database entities directly to API clients?
**Answer**: Exposing entities can leak sensitive data, creates strong coupling between database schemas and APIs, and can trigger lazy initialization exceptions during JSON serialization.

#### Q50: How does Hibernate implement optimistic locking?
**Answer**: Hibernate tracks a version column in each record. If two transactions attempt to update the same row, the first transaction succeeds and increments the version. The second transaction fails and throws an `OptimisticLockException`.

</details>

<details>
<summary><b>Category 3: Security & WebSockets (Questions 51 - 75)</b></summary>

#### Q51: What are the three parts of a JSON Web Token (JWT)?
**Answer**: Header (defines algorithm and type), Payload (contains user claims), and Signature (verifies integrity).

#### Q52: Why is stateless session management preferred in modern web applications?
**Answer**: Stateless architectures do not store session state on servers, allowing APIs to scale horizontally behind load balancers.

#### Q53: What is the purpose of a Security Filter Chain in Spring Security?
**Answer**: A chain of servlet filters that intercepts incoming requests to handle authentication, authorization, CSRF protection, and header validations.

#### Q54: What is the difference between Authentication and Authorization?
**Answer**: Authentication verifies who a user is. Authorization determines what permissions and resources the user is allowed to access.

#### Q55: How does BCrypt work under the hood?
**Answer**: It uses a cost parameter to scale hashing complexity and appends a secure salt to protect against rainbow table attacks.

#### Q56: Why do we store a hashed password instead of plain text?
**Answer**: Hashing prevents passwords from being compromised if the database is exposed in a data breach.

#### Q57: How does the client authenticate subsequent API requests after login?
**Answer**: By including the JWT in the HTTP Request Header: `Authorization: Bearer <token>`.

#### Q58: What is the purpose of a Refresh Token?
**Answer**: It is a secure token used to request a new short-lived access token without requiring the user to re-enter credentials.

#### Q59: Why do we use `OncePerRequestFilter` instead of standard servlet filters?
**Answer**: It guarantees the filter runs exactly once per request, preventing redundant processing.

#### Q60: How does Spring Security handle access denied exceptions?
**Answer**: It delegates them to custom exception entry points (like `RestAuthenticationEntryPoint`), which return clean JSON error payloads.

#### Q61: What is the role of `SecurityContextHolder`?
**Answer**: A helper class that provides thread-local access to the current authenticated security principal.

#### Q62: How does WebSocket communication begin?
**Answer**: The client sends an HTTP request containing upgrade headers. The server switches protocol and upgrades the connection to TCP.

#### Q63: What subprotocol is used over WebSockets in this project?
**Answer**: **STOMP** (Simple Text Oriented Messaging Protocol), which defines standard command frames (CONNECT, SUBSCRIBE, SEND) for message routing.

#### Q64: How does Spring Boot distribute WebSocket messages?
**Answer**: It uses an in-memory message broker (configured using `/topic` and `/queue` prefixes) to route messages to active subscribers.

#### Q65: What is the difference between `/topic` and `/queue` destinations in WebSockets?
**Answer**: `/topic` is used for broadcasting messages to all subscribers. `/queue` is used for point-to-point communication with specific users.

#### Q66: Why did you write a custom STOMP client in the React frontend?
**Answer**: Writing a custom client removes the need for bulky external libraries, simplifies integration with standard browser WebSockets, and allows custom reconnect logic.

#### Q67: What does the `\u0000` character represent in STOMP frames?
**Answer**: The null byte, which is used to mark the end of a STOMP frame payload.

#### Q68: How do you handle authorization for WebSocket connections?
**Answer**: By checking authentication parameters during the initial HTTP upgrade handshake or parsing tokens in the connection frames.

#### Q69: Why do we use Transactional Event Listeners with WebSockets?
**Answer**: Using `@TransactionalEventListener` ensures WebSocket messages are only sent *after* database transactions commit, preventing inconsistent states.

#### Q70: How does `@Async` support event listeners?
**Answer**: It executes the listener method in a background thread, preventing blocking on the main controller request thread.

#### Q71: What is the difference between REST and WebSockets?
**Answer**: REST is stateless and uses a request-response model. WebSockets provide persistent, stateful, bi-directional communication channels.

#### Q72: How are WebSocket messages broadcast to client applications?
**Answer**: By injecting `SimpMessagingTemplate` in services and calling its message publishing methods.

#### Q73: What is the benefit of using WebSockets for notification delivery?
**Answer**: WebSockets provide immediate delivery, avoiding the overhead of client polling.

#### Q74: How does the system handle lost WebSocket connections?
**Answer**: The custom client monitors connection events and triggers automatic reconnect procedures.

#### Q75: Can you scale WebSockets horizontally?
**Answer**: Yes, by using an external message broker (like RabbitMQ or ActiveMQ) to coordinate messages across multiple server instances.

</details>

<details>
<summary><b>Category 4: React, Integrations & Systems Architecture (Questions 76 - 100)</b></summary>

#### Q76: What is React Context, and how does your project use it?
**Answer**: React Context manages global state. We use it to store and share user authentication states and login workflows across components.

#### Q77: What is the purpose of Axios interceptors?
**Answer**: Interceptors modify outgoing requests (e.g., adding authorization headers) and handle incoming responses (like redirecting on 401 errors).

#### Q78: How does the frontend handle token expiration?
**Answer**: When an API request returns a 401 error, the response interceptor triggers token refresh operations or redirects the user to the login screen.

#### Q79: What is the difference between `useMemo` and `useCallback`?
**Answer**: `useMemo` caches computed values. `useCallback` caches function definitions to prevent unnecessary re-renders.

#### Q80: How does local storage support session persistence?
**Answer**: It stores access tokens, refresh tokens, and user profile data in the browser to maintain login sessions across tab reloads.

#### Q81: What is the role of Stripe webhooks in your payment system?
**Answer**: Webhooks receive asynchronous notifications from Stripe to confirm when customer card payments succeed or fail.

#### Q82: How does the backend secure Stripe webhooks?
**Answer**: By validating the payload signature header using Stripe's webhook signing secret.

#### Q83: Why are images uploaded to Cloudinary instead of the backend database?
**Answer**: Storing images in databases causes size issues and performance bottlenecks. Cloudinary offloads file traffic and optimizes image delivery.

#### Q84: What is a Stripe PaymentIntent?
**Answer**: A Stripe resource that tracks the lifecycle of a customer checkout transaction.

#### Q85: How does the application handle missing configurations during startup?
**Answer**: It runs in sandbox mode, using mock values to support local testing when keys are missing.

#### Q86: What is a DTO, and why is it used?
**Answer**: A Data Transfer Object that structures payload data, decoupling the API from the database entities.

#### Q87: What is MapStruct, and what are its advantages?
**Answer**: An annotation processor that generates object mapping code at compile time, avoiding reflection overhead.

#### Q88: Why should you avoid using `RuntimeException` for all errors?
**Answer**: Custom exceptions improve error handling by providing specific status codes and messages in API responses.

#### Q89: How does the global exception handler work?
**Answer**: It uses `@RestControllerAdvice` to intercept exceptions thrown by controllers and format them into clean API responses.

#### Q90: What is the difference between PUT and PATCH?
**Answer**: PUT replaces a resource completely. PATCH applies partial modifications to an existing resource.

#### Q91: Why use UUIDs for Stripe sandbox transaction tracking?
**Answer**: UUIDs provide unique identifiers that prevent ID conflicts in mock databases.

#### Q92: How does `application.yml` distinguish between production and sandbox modes?
**Answer**: By checking if API keys are configured and setting the system mode accordingly.

#### Q93: What is the purpose of the `.env` file in the frontend?
**Answer**: It stores environment-specific variables (like backend API URLs) to prevent hardcoding configuration settings.

#### Q94: Why is spatial indexing recommended for coordinate columns?
**Answer**: Spatial indexing optimizes queries that calculate distances, preventing slow full-table scans.

#### Q95: How does the application prevent duplicate registrations?
**Answer**: By normalizing emails to lowercase and enforcing unique constraints on the email column.

#### Q96: What is the role of Jackson in Spring Boot?
**Answer**: It is the default JSON processing library used to serialize and deserialize data.

#### Q97: What is the purpose of the HikariCP connection pool?
**Answer**: It manages database connections, reducing the overhead of opening and closing connections.

#### Q98: How do you protect endpoints from denial-of-service attacks?
**Answer**: By implementing rate limiting filters using libraries like Bucket4j to restrict request volume.

#### Q99: Why do we use `@Async` for notification event listeners?
**Answer**: It processes notifications in background threads, keeping the main request threads fast.

#### Q100: How do you verify the integrity of the project guide?
**Answer**: By validating the markdown formatting and confirming that all links and file paths are correct.

</details>

---

### Top 50 Interviewer Cross-Questions

This section prepares you for follow-up questions where an interviewer challenges your design decisions:

<details>
<summary><b>Questions 1 - 10: Hibernate & JPA Internals</b></summary>

#### Q1: "You used `@ManyToMany(fetch = FetchType.EAGER)` on the User Roles relationship. Why not LAZY?"
- **Interviewer's Goal**: To see if you understand the risks of Eager loading versus the LazyInitializationException.
- **Your Answer**: "I chose EAGER because roles are small and accessed on every request to authorize API calls. If left as LAZY, accessing roles in security filters outside the transactional boundary would throw a `LazyInitializationException`."
- **Follow-up**: "What if a user has thousands of roles?"
- **Response**: "In that case, EAGER would cause performance issues. I would change it to LAZY, open a transaction in the security filter, or use a custom join query in `CustomUserDetailsService` to fetch roles efficiently."

#### Q2: "Why did you use `GenerationType.IDENTITY` for primary keys? Doesn't it hurt batch insert performance?"
- **Answer**: "Yes, `IDENTITY` prevents Hibernate from using JDBC batch inserts because Hibernate must execute each SQL insert immediately to retrieve the database-generated ID. For this marketplace, writes are user-driven and low-volume, so identity generation is fine. If high-volume batch inserts were required, I would switch to `GenerationType.SEQUENCE` (with a sequence generator) to support batch operations."

#### Q3: "You used `@Transactional` on service methods. What happens if a method calls another `@Transactional` method in the same class?"
- **Answer**: "Spring's transaction management relies on AOP proxies. When a method calls another method within the same class, it bypasses the proxy and calls the method directly. This is called **self-invocation**, and it means annotations on the second method (like `REQUIRES_NEW`) are ignored."
- **Follow-up**: "How do you fix this?"
- **Response**: "By refactoring the code to place the second method in a separate bean, or by injecting the proxy bean self-referentially."

#### Q4: "Why use `open-in-view: false` in your configuration? What problem does it solve?"
- **Answer**: "Setting `open-in-view: false` closes the database connection and transaction as soon as the service layer completes. This prevents connection starvation by ensuring connections are returned to the pool immediately instead of being held open during JSON serialization in the view layer."

#### Q5: "How does JPA's `merge()` handle detached entities under the hood?"
- **Answer**: "When `merge()` is called, Hibernate checks the L1 cache for an entity with the same ID. If not found, it queries the database to load the current state into the persistence context. It then copies the field values from the detached object onto the managed instance and returns it."

#### Q6: "What is the difference between optimistic and pessimistic locking, and when would you use each?"
- **Answer**: "Optimistic locking uses a version column and is suitable for low-contention environments. Pessimistic locking applies database-level locks (like `SELECT FOR UPDATE`) and is used in high-contention systems to prevent write conflicts."

#### Q7: "Why did you use BigDecimal for prices instead of Double?"
- **Answer**: "Double uses floating-point math which can introduce rounding errors. `BigDecimal` provides exact precision, which is required for financial calculations."

#### Q8: "What is the difference between `Session.evict()` and `Session.clear()` in Hibernate?"
- **Answer**: "`evict()` detaches a single entity from the persistence context. `clear()` detaches all entities, clearing the L1 cache."

#### Q9: "How does the `@DynamicUpdate` annotation optimize database operations?"
- **Answer**: "It tells Hibernate to generate SQL UPDATE statements containing only the columns that actually changed, rather than updating all columns in the table."

#### Q10: "Why does Hibernate require a zero-argument constructor?"
- **Answer**: "Hibernate uses Java Reflection to instantiate entities when loading rows from the database. It requires a default no-argument constructor to create instances before populating fields."

</details>

<details>
<summary><b>Questions 11 - 20: Security & Session Management</b></summary>

#### Q11: "Why did you choose stateless JWT authentication instead of stateful sessions?"
- **Answer**: "Stateless JWT authentication avoids storing session state on the server. This allows the backend to scale horizontally because any server instance can validate requests without sharing session data."

#### Q12: "If JWT is stateless, how do you revoke a token if it is compromised?"
- **Answer**: "You cannot revoke a JWT directly without adding state. To handle revocations, we implement a blacklist in Redis. When a token is flagged, its identifier is stored in Redis for its remaining lifetime, and the security filter checks this list before authorizing requests."

#### Q13: "What is Refresh Token Theft, and how does your project protect against it?"
- **Answer**: "Refresh token theft occurs if a token is intercepted by an attacker. We protect against this using **Refresh Token Rotation (RTR)**. Every time a refresh token is used, it is revoked and a new pair is issued. If a revoked token is used, the system flags it as an attack and revokes all active tokens for that user."

#### Q14: "Why store refresh tokens in the database instead of the frontend?"
- **Answer**: "Storing refresh tokens in the database allows the server to manage sessions, revoke access, and implement rotation policies to protect against token reuse."

#### Q15: "Why configure CORS origin patterns with wildcards instead of listing origins explicitly?"
- **Answer**: "During development, port assignments can change. In production, we restrict origins to specific domain names to prevent unauthorized cross-origin requests."

#### Q16: "What is the role of the `BCryptPasswordEncoder` strength parameter?"
- **Answer**: "It sets the log rounds cost factor. Increasing the cost makes the hashing process slower, protecting against brute-force attacks by increasing the computing power required."

#### Q17: "How do you protect your API endpoints against brute-force password attempts?"
- **Answer**: "By implementing rate-limiting filters (using Bucket4j) that block requests from IP addresses or accounts after repeated login failures."

#### Q18: "What is the danger of storing JWTs in browser LocalStorage?"
- **Answer**: "LocalStorage is accessible via JavaScript, making tokens vulnerable to Cross-Site Scripting (XSS) attacks. In production, we store tokens in secure, HttpOnly cookies."

#### Q19: "Why does the login response include the user profile object?"
- **Answer**: "It reduces API round-trips by providing the user details required to initialize the frontend UI during authentication."

#### Q20: "How does the custom SecurityEntryPoint handle authentication errors?"
- **Answer**: "It intercepts authentication exceptions thrown by filters and writes a clean JSON error response directly to the HTTP output stream."

</details>

<details>
<summary><b>Questions 21 - 30: WebSockets & Spatial Queries</b></summary>

#### Q21: "Why use WebSockets instead of Server-Sent Events (SSE) for notifications?"
- **Answer**: "WebSockets support bi-directional, full-duplex communication, which is required for features like real-time chat widgets. If we only needed server-to-client notifications, SSE would be a simpler alternative."

#### Q22: "How does STOMP over WebSockets handle message routing?"
- **Answer**: "STOMP defines routing destinations (like `/topic` or `/queue`). Spring's message broker uses these destinations to route payloads to active client subscriptions."

#### Q23: "What is the purpose of the heartbeat configuration in WebSockets?"
- **Answer**: "Heartbeats send periodic ping/pong messages to detect and close dead TCP connections, freeing server resources."

#### Q24: "Why did you implement a custom WebSocket client in the frontend?"
- **Answer**: "It removes the dependency on large libraries, simplifies WebSocket integrations, and provides control over connection and subscription logic."

#### Q25: "How does the spatial query find nearby users?"
- **Answer**: "It uses a native SQL query implementing the Haversine formula to compute great-circle distances between coordinate points."

#### Q26: "Why use native queries instead of JPQL for spatial math?"
- **Answer**: "Standard JPQL lacks built-in trigonometry functions (like `acos` or `cos`). Native queries let us leverage database-specific math libraries directly."

#### Q27: "What is the index strategy for spatial coordinates?"
- **Answer**: "We define indexes on latitude and longitude columns to optimize bounding-box calculations, preventing slow table scans during spatial queries."

#### Q28: "How does `SimpMessagingTemplate` target specific users?"
- **Answer**: "It routes messages to user-specific destinations (like `/user/queue/notifications`). Spring resolves these paths using the authenticated user's principal name."

#### Q29: "What happens if a client disconnects while a WebSocket message is being sent?"
- **Answer**: "The server catches the socket write exception, logs the event, and closes the connection. To prevent message loss, we queue critical alerts in the database."

#### Q30: "Why use `@Async` for WebSocket broadcast operations?"
- **Answer**: "Broadcasting involves network operations that can block threads. Using `@Async` handles broadcasts in background pools, keeping client request threads fast."

</details>

<details>
<summary><b>Questions 31 - 40: System Design & External Services</b></summary>

#### Q31: "How do you verify Stripe webhook authenticity?"
- **Answer**: "By passing the raw request payload and signature header to Stripe's SDK validator, which checks them against our webhook secret key."

#### Q32: "What is the fallback strategy if the Stripe API is down?"
- **Answer**: "The application runs in sandbox mode, using mock values to process checkouts and allow development to continue."

#### Q33: "Why does the database store Stripe payment intent IDs?"
- **Answer**: "Storing the intent IDs allows us to map Stripe webhook events back to our database records to confirm and update payment statuses."

#### Q34: "Why use Cloudinary for images instead of local storage?"
- **Answer**: "Local storage does not scale across multiple server instances. Cloudinary provides global, optimized image delivery using its Content Delivery Network (CDN)."

#### Q35: "How do you delete listing images from Cloudinary when listings are removed?"
- **Answer**: "We trigger an asynchronous task that calls Cloudinary's API to delete the image asset using its stored public ID."

#### Q36: "Why define DTO classes as records instead of standard classes?"
- **Answer**: "Java records are immutable, define getters and constructors automatically, and are designed for simple data transfer payloads."

#### Q37: "Why avoid exposing database entities directly in controllers?"
- **Answer**: "Exposing entities creates coupling between database schemas and APIs, exposes sensitive data, and can trigger lazy initialization errors during serialization."

#### Q38: "What is the advantage of MapStruct over ModelMapper?"
- **Answer**: "MapStruct generates standard Java code at compile time, avoiding the reflection overhead and performance bottlenecks of runtime mapping libraries."

#### Q39: "Why create a custom Exception handler instead of using default Spring error pages?"
- **Answer**: "Custom handlers intercept exceptions to return clean, standardized JSON response payloads to the client application."

#### Q40: "Why use `Instant` instead of `LocalDateTime` for database timestamps?"
- **Answer**: "`Instant` stores timestamps in UTC, avoiding timezone alignment issues across distributed servers and databases."

</details>

<details>
<summary><b>Questions 41 - 50: Advanced Scalability</b></summary>

#### Q41: "How would you handle spatial queries for 10 million active users?"
- **Answer**: "I would migrate coordinate columns to MySQL's spatial types (like `POINT`), create a `SPATIAL` index, and query locations using geo-containment functions (like `ST_Distance_Sphere`)."

#### Q42: "How would you handle high database write volumes?"
- **Answer**: "By setting up database replicas to route read operations to secondary nodes, freeing the primary node to handle write operations."

#### Q43: "How does caching improve database performance?"
- **Answer**: "Caching stores frequently accessed data (like categories) in Redis, reducing database query volumes and response latency."

#### Q44: "How does rate limiting protect APIs?"
- **Answer**: "It uses token bucket algorithms to limit the volume of requests from specific IP addresses, preventing denial-of-service attempts."

#### Q45: "How do you scale WebSocket connections horizontally?"
- **Answer**: "By connecting servers to a shared Redis Pub/Sub or RabbitMQ broker, which coordinates and broadcasts messages across all active nodes."

#### Q46: "Why use message queues for notifications?"
- **Answer**: "Queues (like RabbitMQ) decouple notification generation from delivery, ensuring alerts are stored and retried if delivery systems fail."

#### Q47: "What is database connection pool starvation?"
- **Answer**: "It occurs when all pool connections are held open by slow processes, blocking new requests. We prevent it by optimizing transaction boundaries."

#### Q48: "How do database indexes speed up queries?"
- **Answer**: "Indexes create searchable B-Tree structures of column values, allowing the engine to find records quickly without scanning whole tables."

#### Q49: "Why use HttpOnly cookies for JWT storage in production?"
- **Answer**: "`HttpOnly` cookies are inaccessible to JavaScript, protecting tokens from theft via Cross-Site Scripting (XSS) vulnerabilities."

#### Q50: "How do you manage application secrets in production?"
- **Answer**: "We store secret keys in environment variables or external vault services (like HashiCorp Vault) rather than committing them to source code repositories."

</details>

---

### File-by-File Interview Q&As (Core Components)

This section contains file-specific technical questions designed to check your understanding of the core files in the marketplace codebase:

<details>
<summary><b>1. User.java (JPA Entity)</b></summary>

#### Q1: Why is `@Column(unique = true)` in JPA not always sufficient for enforcing uniqueness in production?
**Answer**: The `@Column(unique = true)` annotation is primarily used by Hibernate's schema generator (DDL) to add a unique constraint to the physical database schema during auto-generation. However, if the schema is managed manually (e.g., via Flyway/Liquibase) and the database constraint is missing, `@Column(unique = true)` does not do runtime validation in Java. In addition, concurrent requests can bypass service-level uniqueness checks (like `existsByEmail`) due to race conditions. Therefore, a database-level `UNIQUE` index is required to enforce integrity, coupled with service-layer validation.

#### Q2: Why does Hibernate throw a `LazyInitializationException` when referencing a lazy collection like roles outside of a `@Transactional` service, and how does EAGER fetch prevent this?
**Answer**: Hibernate manages entities within the boundary of a session (Persistence Context). When a `@Transactional` method finishes, the session is closed, and the entities become detached. If a collection is marked as `LAZY`, Hibernate injects a proxy class (e.g., `PersistentBag`) rather than fetching the records. If your code accesses this collection later (e.g., during JSON serialization in the controller or a filter), the proxy tries to load data from the closed session, triggering `LazyInitializationException`. Marking the relationship as `EAGER` instructs Hibernate to run a SQL `JOIN` or immediate secondary select to fetch the data before the transaction and session close.

</details>

<details>
<summary><b>2. SecurityConfig.java (Spring Security Configuration)</b></summary>

#### Q3: What is the role of `DelegatingFilterProxy` and `FilterChainProxy` in Spring Security?
**Answer**: The servlet container (like Apache Tomcat) manages its own servlet filter lifecycle and does not understand Spring's Application Context or its dependency injection system. To bridge this gap, Spring Security registers a servlet filter named `DelegatingFilterProxy`. When requests arrive, the servlet container hands them to this proxy, which delegates the work to a Spring-managed bean named `FilterChainProxy`. This bean then routes the requests through the custom security filter chain (configured in `SecurityConfig.java`) including our JWT filter.

#### Q4: Why must we disable CSRF protection when using stateless JWT authentication?
**Answer**: CSRF (Cross-Site Request Forgery) attacks exploit the browser's default behavior of automatically attaching session cookies to cross-site requests. If authentication relies on stateful `JSESSIONID` cookies, an attacker can trick a logged-in user into clicking a malicious link that submits a form to the backend, using the user's credentials automatically. When using stateless JWTs, the token is typically passed via the custom HTTP header `Authorization: Bearer <token>`. Browsers do not automatically attach custom headers to cross-site requests, which protects the API from CSRF, making CSRF tokens redundant.

</details>

<details>
<summary><b>3. JwtAuthenticationFilter.java (Servlet Filter)</b></summary>

#### Q5: Why does `JwtAuthenticationFilter` extend `OncePerRequestFilter` instead of implementing standard servlet `Filter` directly?
**Answer**: A standard servlet request can traverse the filter chain multiple times (for example, during forwards, includes, or error dispatches). If a filter implements the basic `javax.servlet.Filter` interface directly, its `doFilter` method might execute multiple times for a single request, causing redundant token parsing and database lookups. `OncePerRequestFilter` is a Spring-specific helper class that guarantees the filter is executed exactly once per request container dispatch thread by checking a request attribute flag.

#### Q6: What is the risk of using `SecurityContextHolder.getContext().setAuthentication(...)` without clearing it, and how does the request thread model affect this?
**Answer**: Tomcat uses a thread-per-request model where worker threads are reused from a pool to process incoming HTTP requests. Spring's `SecurityContextHolder` uses a `ThreadLocal` strategy under the hood to store authentication details. If a filter sets the security context but fails to clean it up, the next request processed by that same worker thread might inherit the security context of the previous user (privilege escalation). While Spring Security's `FilterChainProxy` automatically clears the context at the end of the request cycle, custom filters must exercise caution and avoid manually modifying the thread-local state outside the security context pipeline.

</details>

<details>
<summary><b>4. AuthService.java (Business Logic Service)</b></summary>

#### Q7: What is the difference between physical rollback and logical rollback in Spring's `@Transactional`?
**Answer**: When a transaction is marked `@Transactional` and a runtime exception is thrown, Spring's transaction interceptor marks the transaction as "rollback-only". If this method is called within an outer transaction (propagation `REQUIRED`), the outer transaction cannot commit even if it catches the exception, because the logical transaction state has been marked for rollback. A physical rollback is the database-level `ROLLBACK` command executed on the JDBC connection, whereas a logical rollback is the Spring transaction manager state marking that prevents commit completion.

#### Q8: How does BCrypt prevent rainbow table attacks, and why is `BCrypt.checkpw` needed instead of standard string comparison?
**Answer**: BCrypt generates a random salt for every password before hashing and embeds this salt inside the final hashed string. A rainbow table is a precomputed table of plaintext passwords and their hashes. Since every hash uses a unique random salt, attackers cannot use precomputed tables to crack hashes in bulk. When verifying a password, `BCrypt.checkpw` extracts the salt from the stored hash, applies it to the input plaintext password, hashes it, and compares the resulting hash. A standard string comparison won't work because simple hashing would produce a different hash without the salt extraction step.

</details>

<details>
<summary><b>5. AuthController.java (REST Controller)</b></summary>

#### Q9: How does the `@Valid` annotation trigger input validation, and how is it processed in Spring MVC?
**Answer**: When a request payload is deserialized into a DTO, the `RequestResponseBodyMethodProcessor` in Spring MVC checks for the `@Valid` or `@Validated` annotation. If present, it delegates validation to the configured JSR-383 validator (Hibernate Validator). The validator inspects constraint annotations (e.g., `@Email`, `@NotBlank`) on the DTO. If any validation fails, it throws a `MethodArgumentNotValidException`. The framework then routes this exception to the global exception handler instead of proceeding to the controller method.

#### Q10: Why should we use specialized Request and Response DTOs instead of returning the raw JPA entities from controller endpoints?
**Answer**: Returning raw JPA entities leaks database schema structures to API clients, couples the API contract to database tables, and can trigger unwanted lazy loading queries during JSON serialization (leading to N+1 queries or serialization errors). DTOs allow us to select exactly which fields are exposed, apply specific validation rules for different operations (e.g., login vs registration), and decouple internal models from external client requirements.

</details>

<details>
<summary><b>6. FoodListing.java (JPA Entity)</b></summary>

#### Q11: Why is it critical to synchronize both sides of a bidirectional association (like `@OneToMany` and `@ManyToOne`), and how is this implemented?
**Answer**: In a bidirectional relationship, Java objects are not automatically synchronized. If you add a child image to the parent listing's list but forget to set the listing reference on the image, the image will be saved with a null foreign key (or fail database constraints) because the child side is the owner of the relationship (`mappedBy` is on the parent). To prevent this, we write helper methods like `addImage(FoodListingImage image)` which adds the image to the parent's list and sets the parent reference on the image at the same time: `image.setListing(this)`.

#### Q12: What is the purpose of `orphanRemoval = true` in `@OneToMany`, and how does it differ from `CascadeType.REMOVE`?
**Answer**: `CascadeType.REMOVE` propagates the deletion of the parent entity to all its children. If you delete a `FoodListing`, all its images are deleted. However, if you simply remove an image from the parent listing's collection (`listing.getImages().remove(image)`), `CascadeType.REMOVE` does nothing, leaving the child record as an orphan in the database with a null foreign key. `orphanRemoval = true` tells Hibernate that if a child entity is dissociated from the parent, it should be physically deleted from the database automatically.

</details>

<details>
<summary><b>7. FoodListingService.java (Domain Service)</b></summary>

#### Q13: Why is publishing events (`ApplicationEventPublisher.publishEvent`) inside a `@Transactional` method a potential risk, and how do we mitigate it?
**Answer**: By default, Spring application events are synchronous. If you publish an event inside a transactional method, the event listener runs on the same thread within the same database transaction. If the listener fails or takes a long time, it can roll back the database transaction or hold the connection open. If the listener sends an email or WebSocket broadcast, it might execute before the transaction commits. If the transaction eventually rolls back, the user receives an email about a listing that does not exist. We mitigate this by using `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` to ensure the listener only runs after successful database commit.

#### Q14: How does Spring Boot handle method-level security checks like `@PreAuthorize("hasRole('BUSINESS')")`?
**Answer**: Spring Security uses Spring AOP proxies. When a class is annotated with `@EnableMethodSecurity` and a method has `@PreAuthorize`, Spring creates a security proxy around the service bean. When the method is invoked, the proxy intercepts the call, reads the `SecurityContextHolder` to fetch the current user's authorities, evaluates the SpEL (Spring Expression Language) expression, and either allows execution or throws an `AccessDeniedException`.

</details>

<details>
<summary><b>8. UserRepository.java (Spatial Query Repository)</b></summary>

#### Q15: How does the database calculate the Haversine formula, and why is a `nativeQuery = true` needed here?
**Answer**: The Haversine formula uses trigonometric functions (like `acos`, `cos`, `sin`) to calculate great-circle distances. Standard JPQL (Java Persistence Query Language) does not support mathematical and trigonometric functions in a database-independent way. By setting `nativeQuery = true`, we instruct Spring Data JPA to pass the SQL string directly to the underlying MySQL database, allowing us to leverage MySQL's native math capabilities.

#### Q16: What is the difference between a `nativeQuery` and a JPQL query regarding entity lifecycle management?
**Answer**: When executing a JPQL query, Hibernate manages the returned entities in the persistence context (L1 cache), monitoring them for dirty checks. When executing a native query that selects all columns (`SELECT u.*`), Hibernate also maps the rows to entities and manages them. However, if the native query returns arbitrary columns (projection) instead of the full entity columns, the result is returned as raw objects (`Object[]`) or custom DTO interfaces, which are read-only and not managed in the persistence context.

</details>

<details>
<summary><b>9. TransactionEventListener.java (Event Listener)</b></summary>

#### Q17: Why must methods annotated with `@Async` be placed in a separate class or invoked through a proxy?
**Answer**: Like `@Transactional`, `@Async` relies on AOP proxies. When you call an `@Async` method from another method within the same class (self-invocation), the call bypasses the Spring proxy and runs synchronously on the caller's thread. To ensure asynchronous execution, the `@Async` method must be placed in a separate bean so that calls route through the Spring AOP proxy, which submits the task to a thread pool (`TaskExecutor`).

#### Q18: What happens to the execution thread pool when a large volume of asynchronous events are published?
**Answer**: Spring uses a `TaskExecutor` thread pool (configured in application properties or defaults). When an event is published, a worker thread is fetched from the pool to execute the listener. If the pool is exhausted and the queue is full, Spring can reject the task or run it on the caller thread depending on the configured rejection policy. It is important to configure core pool sizes, max pool sizes, and queue capacities to match expected notification volumes.

</details>

<details>
<summary><b>10. StripePaymentService.java (Payment Service)</b></summary>

#### Q19: What is a Stripe Webhook, and why is signature verification (`Signature.verifyHeader`) mandatory?
**Answer**: A Stripe Webhook is an HTTP POST request sent by Stripe to our backend endpoint to notify us of asynchronous events, such as a payment succeeding or failing. Because this endpoint is public, anyone could send a fake payload pretending that a payment succeeded. Signature verification uses a shared webhook secret to verify that the payload signature matches the hash of the payload, confirming it was sent by Stripe and not modified in transit.

#### Q20: Why do we convert financial amounts to cents (e.g., multiplying by 100) before sending them to Stripe?
**Answer**: To prevent floating-point rounding errors that occur when representing currency values in decimals (such as float or double). Stripe's API requires all transaction amounts to be in the smallest currency unit (e.g., cents for USD, pence for GBP), ensuring arithmetic precision.

</details>

<details>
<summary><b>11. SimpleStompClient.js (React WebSocket Client)</b></summary>

#### Q21: Why do we use the STOMP protocol over raw WebSockets, and what are its core frames?
**Answer**: Raw WebSockets only establish a bi-directional TCP pipe without defining how messages should be structured or routed. STOMP (Simple Text Oriented Messaging Protocol) is a subprotocol that adds structure using command frames. Core frames include: `CONNECT` (initiates connection), `CONNECTED` (server ack), `SUBSCRIBE` (registers destination), `SEND` (publishes message), and `MESSAGE` (delivers message to subscribers).

#### Q22: How does the WebSocket handshake work, and how is it upgraded from HTTP?
**Answer**: The connection starts with a standard HTTP GET request containing upgrade headers: `Connection: Upgrade` and `Upgrade: websocket`. If the server supports WebSockets, it responds with HTTP status `101 Switching Protocols`. The TCP socket connection remains open, and the protocol switches from HTTP request-response to WebSocket full-duplex communication.

</details>

---

### Common Mistakes Students Make

This section lists common development mistakes and how to avoid them:

#### 1. Transactional AOP Self-Invocation
* **Mistake**: Calling a `@Transactional` method from another method in the same class. Since Spring uses proxy wrappers to manage transactions, calling a method internally bypasses the proxy, meaning the transaction is not opened.
* **Fix**: Place the transactional method in a separate service bean, or inject the service bean self-referentially to route calls through the proxy wrapper.

#### 2. LazyInitializationException in the View Layer
* **Mistake**: Accessing lazy relationship collections (e.g., listing images) in controllers or views after the service transaction has closed.
* **Fix**: Set `spring.jpa.open-in-view=false` to detect these issues during testing, and retrieve lazy data using fetch joins or custom query methods.

#### 3. Missing Webhook Signature Verification
* **Mistake**: Processing Stripe payment notifications without verifying the signature header, allowing attackers to forge payment success events.
* **Fix**: Always validate incoming webhook payloads against your Stripe signing secret key.

#### 4. Storing Secret Keys in Source Repositories
* **Mistake**: Committing database passwords and API keys to Git repositories.
* **Fix**: Use environment placeholders (like `${JWT_SECRET}`) in configuration files and load secrets from environment variables.

---

### Production Scaling to 1 Million Users

If this application were scaled to support 1 million active users, the architecture would be upgraded as follows:

```
                  [ Client Applications (React Web / Mobile) ]
                                      │
                                      ▼
                      [ DNS & Cloudflare CDN (SSL/WAF) ]
                                      │
                                      ▼
                        [ NGINX Load Balancer (Layer 7) ]
                                      │
               ┌──────────────────────┼──────────────────────┐
               ▼                      ▼                      ▼
        [ API Server 1 ]       [ API Server 2 ]       [ API Server 3 ] (Horizontal Scaling)
               │                      │                      │
               ├──────────────────────┴──────────────────────┤
               ▼                                             ▼
     [ Redis Cache Cluster ]                       [ RabbitMQ Message Queue ]
  (Reads, Sessions, Rate Limits)                 (Notification & Mail Workers)
               │                                             │
               ▼                                             ▼
   [ MySQL Primary (Writes) ]                      [ Cloudinary Image CDN ]
               │
      ┌────────┴────────┐ (Replication)
      ▼                 ▼
[ MySQL Replica 1 ] [ MySQL Replica 2 ] (Reads)
```

1. **Database Replication**: Set up a primary MySQL node to handle writes and database updates, and configure replica nodes to serve read queries.
2. **Caching Layer**: Integrate Redis to cache common read requests (like food categories) and handle rate limiting counters.
3. **Message Queuing**: Implement RabbitMQ or Apache Kafka to process background tasks (like emails and push notifications) asynchronously.
4. **WebSocket Scaling**: Connect backend WebSocket servers using a shared Redis Pub/Sub broker to synchronize messages across instances.
5. **Database Indexing**: Add database indexes to columns used in spatial and coordinate queries to optimize lookup operations.

---

## 9. Interview Pitch & Academic Viva Guide

This section helps you present your project effectively in interviews and academic presentations.

---

### How to Pitch this Project

#### 2-Minute HR Pitch
> "I developed a Real-Time Surplus Food Marketplace using Spring Boot and React. The application helps businesses reduce food waste by selling surplus items at a discount, or donating them to local NGOs. I implemented secure JWT authentication with token rotation, integrated Stripe to process card payments, and created a custom WebSocket STOMP client to send real-time alerts. I also implemented spatial queries to notify nearby users when new items are posted."

#### 5-Minute Technical Pitch
> "This project is a full-stack surplus food marketplace built on Spring Boot, React, and MySQL. The backend is designed with a layered architecture: REST controllers validate inputs, services manage transactions, and Spring Data JPA repositories handle database access.
> I implemented secure JWT authentication with refresh token rotation to protect API endpoints, and integrated Stripe payments with webhook verification to confirm transactions. For real-time updates, I implemented a STOMP WebSocket broker and wrote a lightweight client in React. I also optimized database operations by using fetch joins to prevent N+1 query issues."

---

### Academic Viva Questions

#### Q1: "Why did you choose MySQL instead of MongoDB for this project?"
* **Answer**: "The application manages financial transactions, order lifecycles, and user relations that require ACID guarantees. Relational databases like MySQL support foreign keys, integrity constraints, and transactional consistency, which are critical for financial operations."

#### Q2: "How does the application calculate distances for spatial searches?"
* **Answer**: "The system queries user coordinates using a native SQL query that implements the Haversine formula. It calculates the great-circle distance between two points on a sphere, filtering users located within a 10 km radius."

#### Q3: "What happens if a user submits invalid data in a registration form?"
* **Answer**: "The frontend performs initial format validation. When the payload reaches the API, Spring's validation engine checks the fields against validation annotations, and the global exception handler returns structured JSON error messages to the client."
