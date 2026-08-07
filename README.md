# Enterprise Java Backend REST API: Surplus Food Marketplace

This repository hosts the enterprise-grade, highly scalable **Surplus Food Marketplace Backend** built with **Java 21** and **Spring Boot**. The project serves as a high-performance transactional platform that connects food businesses (restaurants, grocery stores) with consumers and NGOs to redistribute surplus food (either sold at a discount or donated).

The architecture features a sessionless **JWT-based Spring Security filter chain**, database transaction management via **Spring Data JPA/Hibernate**, spatial coordinate indexing for location-based search, an event-driven notification subsystem, real-time message broadcasting over **WebSockets**, and payment gateway integrations.

*(Note: A lightweight React client consumes the exposed backend REST APIs).*

---

## 1. System Architecture Blueprint

The application employs a layered architecture following clean coding principles. The workflow of an incoming request is described below:

```
Client (Thin React UI)
  │ (HTTP Requests with JWT Bearer Token / WebSocket Upgrade)
  ▼
[ Tomcat Servlet Container ] ──► (Assigns Tomcat Worker Thread from Pool)
  │
  ▼
[ Servlet Filter Chain (Spring Security) ]
  ├── JwtAuthenticationFilter (Extracts and validates JWT, parses claims)
  └── SecurityContextHolder (Caches UserPrincipal in ThreadLocal)
        │
        ▼
[ DispatcherServlet (Spring MVC Front Controller) ]
  ├── HandlerMapping (Finds RestController routing handler)
  └── HandlerAdapter (Executes Controller method)
        │
        ▼
[ RestController Layer (e.g., FoodListingController) ]
  ├── Jackson Message Converter (Deserializes JSON to DTO)
  └── JSR-380 Validator (Enforces constraints like @NotBlank, @Min)
        │
        ▼
[ Transaction Interceptor Proxy (AOP Boundary) ]
  ├── Acquires JDBC Connection from HikariCP Pool
  └── Invokes Connection.setAutoCommit(false) to open transaction
        │
        ▼
[ Service Layer (Business Domain Logic) ]
  ├── Applies validation checks and maps DTOs to entities using MapStruct
  └── Publishes internal notifications using ApplicationEventPublisher
        │
        ▼
[ Repository Layer (Spring Data JPA) ]
  ├── Hibernate Session (L1 Cache Context tracks managed entities)
  └── SQL Query Generation (Executes updates, select joins, or native queries)
        │
        ▼
[ Database Instance (MySQL InnoDB Engine) ] ──► (Processes queries, applies locks)
        │
        ▼
[ Transaction Commit & Serialization ]
  ├── Commits transaction (or rolls back on RuntimeException)
  ├── Releases Connection back to HikariCP Pool
  └── Jackson serializes returned object to JSON, writing to Tomcat buffer
```

---

## 2. Directory Structure

The backend source follows standard Maven structures to maintain separation of concerns:

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/surplusfood/marketplace/
│   │   │   ├── SurplusFoodMarketplaceApplication.java # Spring Boot Entrypoint
│   │   │   ├── config/              # Central configurations (Security, CORS, WS, Cache)
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── WebSocketConfig.java
│   │   │   │   └── RoleDataInitializer.java
│   │   │   ├── controller/          # REST Controllers exposing resource mappings
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── FoodListingController.java
│   │   │   │   └── PaymentController.java
│   │   │   ├── dto/                 # Immutable request/response transfer models
│   │   │   │   ├── LoginRequest.java
│   │   │   │   └── FoodListingRequest.java
│   │   │   ├── entity/              # Database entities managed by JPA
│   │   │   │   ├── User.java
│   │   │   │   ├── FoodListing.java
│   │   │   │   └── Order.java
│   │   │   ├── event/               # Domain event schemas
│   │   │   │   └── FoodListingCreatedEvent.java
│   │   │   ├── listener/            # Transactional decoupled event observers
│   │   │   │   └── TransactionEventListener.java
│   │   │   ├── mapper/              # Compile-time MapStruct mapping layer
│   │   │   │   └── FoodListingMapper.java
│   │   │   ├── repository/          # JpaRepositories executing DB queries
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── FoodListingRepository.java
│   │   │   ├── security/            # Security validation filters & JWT services
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   └── service/             # Transactional business logic processing
│   │   │       ├── AuthService.java
│   │   │       ├── FoodListingService.java
│   │   │       └── StripePaymentService.java
│   │   └── resources/
│   │       ├── application.yml      # Configuration properties (active profiles)
│   │       └── schema.sql           # DDL schema definition scripts
│   └── test/                        # Unit and integration test suites
└── pom.xml                          # Maven dependency descriptor
```

---

## 3. Database Schema & Index Optimization

The database is built on the MySQL InnoDB storage engine. Tables use explicit constraints and indexes to optimize lookup speeds under load:

- **Users**: Mapped to `users`. Features a `UNIQUE` index on the `email` column to accelerate authentication checks. Coordinates (`latitude`, `longitude`) are indexed to support spatial lookup queries.
- **Food Listings**: Mapped to `food_listings`. Extends spatial coordinate fields. Includes composite indexes on `(status, listing_type)` to optimize catalog searches.
- **Join Tables**: `user_roles` links users to their system authorizations. The composite key `(user_id, role_id)` prevents duplicate roles and optimizes join lookups.
- **Database Index Optimization Strategy**:
  - Spatial indexes (`SPATIAL INDEX`) are applied on coordinate columns to support geographic queries.
  - Foreign keys are indexed to prevent full-table locks during cascade validations.

---

## 4. Key Architectural Features

### Sessionless JWT Security Chain
Authentication uses stateless JSON Web Tokens. Incoming requests are intercepted by `JwtAuthenticationFilter`, which verifies the signature, extracts user claims, and updates the `SecurityContextHolder`. Password hashing uses the BCrypt algorithm with a cost factor of 12.

### Event-Driven Notifications
To keep transactions fast, the listing service does not send notifications synchronously. Instead, creating a listing publishes a `FoodListingCreatedEvent`. An asynchronous listener annotated with `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` processes the event in a background thread, broadcasting updates via WebSockets and sending emails only after the listing is saved.

### Real-Time WebSocket Communication
The backend configures an in-memory message broker using the STOMP protocol. Sockets are upgraded from standard HTTP request streams to TCP channels. Real-time updates (like new listings or order notifications) are broadcast to subscribers on channels like `/topic/listings` and `/user/queue/notifications`.

### External Services Integration
- **Stripe**: The payment workflow initializes Stripe `PaymentIntent` items in cents to avoid floating-point errors. Verification is completed asynchronously using secure Stripe webhooks, checking signatures before updating database records.
- **Cloudinary**: Handles image uploads. To reduce server load, image metadata (such as URL and public ID) is sent directly from the client to the backend database, offloading file traffic from the JVM.

---

## 5. REST API Reference

Exposes documentation using Swagger UI at `/swagger-ui/index.html`. Key endpoints include:

- `POST /api/auth/register`: Create user account. Returns auth tokens.
- `POST /api/auth/login`: Authenticate credentials. Returns access and refresh token pairs.
- `GET /api/listings/nearby`: Spatial query returning active listings within a specified coordinate radius.
- `POST /api/listings`: Expose a new surplus listing (Verified seller roles only).
- `POST /api/orders`: Reserve surplus food listings.
- `POST /api/payments/webhook`: Process payment updates securely.

---

## 6. Setup & Deployment

### Run using Docker
```bash
# Build package using Maven
mvn clean package -DskipTests

# Build and run containers
docker-compose up --build
```

### Monitoring (Actuator)
Exposes metrics at `/actuator`:
- `/actuator/health`: System health.
- `/actuator/prometheus`: Metrics format compatible with Prometheus and Grafana.
