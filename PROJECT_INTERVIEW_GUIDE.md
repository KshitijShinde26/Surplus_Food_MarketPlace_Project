# Java Backend Project Interview Master Guide: Surplus Food Marketplace

Welcome to the **Surplus Food Marketplace Java Backend Project Interview Master Guide**. This guide is designed to prepare you for senior-level Java backend interviews by mastering your project's architecture, design decisions, and database lifecycles.

This guide consists of exactly **52 Master Questions** structured into **7 Sections**. Each question is broken down into **12 detailed sub-sections**, ensuring you can explain every backend concept from first principles to production implementation.

---

## Table of Contents
1. [Section 1 — Project (Questions 1 - 5)](#section-1--project)
2. [Section 2 — Spring Boot (Questions 6 - 13)](#section-2--spring-boot)
3. [Section 3 — Spring Security (Questions 14 - 21)](#section-3--spring-security)
4. [Section 4 — Hibernate & JPA (Questions 22 - 29)](#section-4--hibernate--jpa)
5. [Section 5 — Database (Questions 30 - 34)](#section-5--database)
6. [Section 6 — Project Features (Questions 35 - 44)](#section-6--project-features)
7. [Section 7 — Production & Scalability (Questions 45 - 52)](#section-7--production--scalability)

---

## Section 1 — Project

### Q1: Explain your project.
- **Why interviewer asks this**: To evaluate your communication style and see if you can summarize a project's purpose and backend stack concisely.
- **Expected Interview Answer**: The **Surplus Food Marketplace** is a scalable Spring Boot REST API that connects food businesses with consumers and NGOs to redistribute surplus food (discounted sales or donations). It features secure JWT-based Spring Security, native spatial queries, transactional event notifications, and Stripe payments. The frontend React client is a thin API consumer.
- **Simple Explanation**: A backend platform that allows restaurants to post food items for sale or donation, let users buy them, and automatically notifies nearby NGOs when food is available.
- **Internal Working**: Integrates MVC controllers, AOP proxies, and Hibernate. Coordinates are indexed spatially, and transactions are synchronized with WebSockets using event listeners.
- **ASCII Flow Diagram**:
  ```
  [React Client] ──(HTTPS)──► [Spring Boot REST API] ──(JPA)──► [MySQL DB]
  ```
- **Related Project Classes**: [SurplusFoodMarketplaceApplication](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/SurplusFoodMarketplaceApplication.java)
- **Related Technologies**: Java 21, Spring Boot, MySQL, Maven
- **Common Follow-up Questions**:
  1. What is the business value of this system?
  2. How do you handle role validation?
  3. Why use spatial data?
- **Common Mistakes**: Spending too much time explaining UI screens instead of backend layers.
- **Production Best Practice**: Package configurations into environment-specific profiles.
- **Real Example from project**: Combines controllers, services, and repositories to process requests.

---

### Q2: Explain the project architecture.
- **Why interviewer asks this**: To see if you understand layered architectures and separation of concerns.
- **Expected Interview Answer**: The project uses a layered architecture: **Controller** (routes and input validation), **Service** (business logic and transactions), **Repository** (data access proxies), and **Entity** (database mapping), ensuring clean boundaries and testability.
- **Simple Explanation**: Code is split into layers: one for routing requests, one for processing business rules, and one for talking to the database.
- **Internal Working**: Data moves between layers using MapStruct mappers and immutable DTOs, keeping database entities isolated from the API contract.
- **ASCII Flow Diagram**:
  ```
  [Controller] ──(DTOs)──► [Service Layer] ──(Entities)──► [Repository Layer]
  ```
- **Related Project Classes**: [FoodListingController](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/controller/FoodListingController.java), [FoodListingService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/FoodListingService.java)
- **Related Technologies**: Spring MVC, JSR-380 validation, MapStruct
- **Common Follow-up Questions**:
  1. Why not call repositories directly from controllers?
  2. Where is validation logic enforced?
  3. How do layers communicate?
- **Common Mistakes**: Exposing raw database entities in controller methods.
- **Production Best Practice**: Make DTO definitions immutable to prevent side effects.
- **Real Example from project**: [FoodListingController](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/controller/FoodListingController.java) receives requests, validates parameters, and forwards logic to [FoodListingService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/FoodListingService.java).

---

### Q3: Why did you choose Spring Boot?
- **Why interviewer asks this**: To evaluate your tooling choices and technical reasoning.
- **Expected Interview Answer**: I chose Spring Boot for its **Auto-Configuration**, **Starter Dependencies**, and built-in **Tomcat container**. It simplifies setup, integrates with Hibernate/JPA, and includes Actuator for production metrics.
- **Simple Explanation**: It handles setup boilerplate automatically, letting developers focus on writing business logic.
- **Internal Working**: Boot scans dependencies at startup and registers required configuration beans conditionally.
- **ASCII Flow Diagram**:
  ```
  [Starter JARs] ──► [Classpath Analyzer] ──► [Auto-Configured Beans]
  ```
- **Related Project Classes**: [pom.xml](file:///c:/Users/kshit/eclipse-workspace/Project/backend/pom.xml)
- **Related Technologies**: Spring Boot Starter Dependencies
- **Common Follow-up Questions**:
  1. What is starter dependency resolution?
  2. How do you exclude specific auto-configurations?
  3. What is the role of spring-boot-maven-plugin?
- **Common Mistakes**: Calling Spring Boot a framework; it is an opinionated packaging utility around the Spring Framework.
- **Production Best Practice**: Keep base dependency versions aligned using the parent POM.
- **Real Example from project**: [pom.xml](file:///c:/Users/kshit/eclipse-workspace/Project/backend/pom.xml) inherits configurations from `spring-boot-starter-parent` version 3.3.5.

---

### Q4: Explain the complete request lifecycle.
- **Why interviewer asks this**: To evaluate your understanding of how web requests move through servlet containers and Spring frameworks.
- **Expected Interview Answer**: Requests hit Tomcat's NIO connector. A worker thread is assigned. The request moves through servlet security filters, is mapped by `DispatcherServlet` to a controller method, executes business logic inside a transaction, and returns serialized JSON.
- **Simple Explanation**: A request goes through security filters, gets routed to a Java method, updates the database, and returns JSON.
- **Internal Working**: The request updates a thread-local security context. Spring's AOP proxy wraps the service method, managing connection borrows and transaction boundaries.
- **ASCII Flow Diagram**:
  ```
  [Tomcat Thread] ──► [Filters] ──► [DispatcherServlet] ──► [Controller] ──► [Service] ──► [DB]
  ```
- **Related Project Classes**: [JwtAuthenticationFilter](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/security/JwtAuthenticationFilter.java), [SecurityConfig](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/SecurityConfig.java)
- **Related Technologies**: Tomcat Web Server, Servlet Specification, Spring MVC
- **Common Follow-up Questions**:
  1. How are Tomcat worker threads allocated?
  2. Where does JSON serialization happen?
  3. How does AOP intercept service calls?
- **Common Mistakes**: Claiming filters execute inside Spring's MVC container; they run at the servlet level before reaching it.
- **Production Best Practice**: Optimize Tomcat's thread pool size to match system hardware.
- **Real Example from project**: [JwtAuthenticationFilter](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/security/JwtAuthenticationFilter.java) intercepts requests early to validate tokens before routing them to controllers.

---

### Q5: Explain the project modules.
- **Why interviewer asks this**: To see if you understand the project's folder layout and separation of concerns.
- **Expected Interview Answer**: The project is split into a **backend** Spring Boot API and a **frontend** React client. The backend handles data persistence, transactions, and security, while the frontend consumes REST APIs.
- **Simple Explanation**: The project is split into two folders: one for server-side logic and database operations, and one for the browser UI.
- **Internal Working**: Build tools package the backend and frontend separately, allowing them to scale independently.
- **ASCII Flow Diagram**:
  ```
  [Project Root] ──┬──► [backend/] (Java API Module)
                   └──► [frontend/] (React Client Module)
  ```
- **Related Project Classes**: [pom.xml](file:///c:/Users/kshit/eclipse-workspace/Project/backend/pom.xml)
- **Related Technologies**: Maven build configurations
- **Common Follow-up Questions**:
  1. How are build steps managed for packaging?
  2. Why keep codebases in a single repository?
  3. How do modules share API specs?
- **Common Mistakes**: Coupling backend and frontend compilation phases directly in maven.
- **Production Best Practice**: Compile and deploy modules independently using multi-stage pipelines.
- **Real Example from project**: The workspace contains a Maven-based `backend/` folder and a package-based `frontend/` folder.

---

## Section 2 — Spring Boot

### Q6: What happens after SpringApplication.run() executes?
- **Why interviewer asks this**: To evaluate your knowledge of Spring's boot lifecycle and initialization sequences.
- **Expected Interview Answer**: It creates the `ApplicationContext` container, starts the embedded Tomcat server, scans classpath components, instantiates singleton beans, runs dependencies injection, and triggers initializers.
- **Simple Explanation**: It boots the application, searches for annotated classes, registers them, and starts the server.
- **Internal Working**: The JVM registers bean definitions, runs post-processors, executes `@PostConstruct` methods, and starts Tomcat connectors.
- **ASCII Flow Diagram**:
  ```
  run() ──► [Create Context] ──► [Scan Beans] ──► [Inject Dependencies] ──► [Start Tomcat]
  ```
- **Related Project Classes**: [SurplusFoodMarketplaceApplication](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/SurplusFoodMarketplaceApplication.java)
- **Related Technologies**: Spring Boot Initializers
- **Common Follow-up Questions**:
  1. How are environment properties loaded?
  2. What is the role of BeanDefinition?
  3. When are initializers executed?
- **Common Mistakes**: Assuming all beans are initialized lazily; Spring loads singleton beans eagerly by default.
- **Production Best Practice**: Validate environment profiles early to fail fast if properties are missing.
- **Real Example from project**: [SurplusFoodMarketplaceApplication](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/SurplusFoodMarketplaceApplication.java) initializes the Spring context when launched.

---

### Q7: Explain Dependency Injection.
- **Why interviewer asks this**: To verify your understanding of core Spring IoC and dependency management principles.
- **Expected Interview Answer**: Dependency Injection (DI) is an implementation of Inversion of Control (IoC). Instead of classes instantiating their dependencies manually, the Spring container manages instantiation, injection, and lifecycles.
- **Simple Explanation**: The framework creates and provides dependency objects to classes automatically, rather than classes creating them manually.
- **Internal Working**: Spring reads metadata, resolves constructor arguments, instantiates components, and cache references.
- **ASCII Flow Diagram**:
  ```
  [Bean Definitions] ──► [IoC Container Instantiation] ──► [Injected Targets]
  ```
- **Related Project Classes**: [FoodListingService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/FoodListingService.java)
- **Related Technologies**: Spring IoC Container
- **Common Follow-up Questions**:
  1. What is the difference between IoC and DI?
  2. What are the types of dependency injection?
  3. How does Spring resolve type conflicts?
- **Common Mistakes**: Creating instances manually using the `new` keyword for Spring-managed classes.
- **Production Best Practice**: Prefer constructor injection to ensure dependencies are immutable and easy to test.
- **Real Example from project**: [FoodListingService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/FoodListingService.java) receives its repositories automatically via constructor injection.

---

### Q8: Why @Service?
- **Why interviewer asks this**: To see if you understand component scanning stereotypic annotations and clean architecture boundaries.
- **Expected Interview Answer**: `@Service` is a stereotypic specialization of `@Component`. It registers the class as a business service bean, making it eligible for classpath scanning and proxy wrapping.
- **Simple Explanation**: An annotation that marks a class as a business logic component, telling Spring to manage it.
- **Internal Working**: Scanned at startup; registers a `BeanDefinition` and wraps methods in AOP proxies if annotated with `@Transactional`.
- **ASCII Flow Diagram**:
  ```
  @Service Class ──► [Component Scan] ──► [Business Logic Bean Registered]
  ```
- **Related Project Classes**: [AuthService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/AuthService.java)
- **Related Technologies**: Spring Stereotype Annotations
- **Common Follow-up Questions**:
  1. How does `@Service` differ from `@Component`?
  2. Can you place `@Transactional` on `@Component`?
  3. How are bean names generated?
- **Common Mistakes**: Placing business transaction logic in controllers instead of service classes.
- **Production Best Practice**: Keep service classes stateless to ensure thread safety across requests.
- **Real Example from project**: [AuthService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/AuthService.java) is annotated with `@Service` to handle user registration and login workflows.

---

### Q9: Why @Component?
- **Why interviewer asks this**: To evaluate your knowledge of generic stereotypic annotations in Spring.
- **Expected Interview Answer**: `@Component` is a generic annotation that registers a class as a Spring-managed bean, making it eligible for auto-detection and dependency injection during classpath scans.
- **Simple Explanation**: A general-purpose annotation used to register utility classes as Spring beans.
- **Internal Working**: Scanned during bootstrap to build a `BeanDefinition` metadata mapping for the container.
- **ASCII Flow Diagram**:
  ```
  @Component Class ──► [Component Scan] ──► [Generic Bean Context Registration]
  ```
- **Related Project Classes**: [JwtAuthenticationFilter](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/security/JwtAuthenticationFilter.java)
- **Related Technologies**: Classpath scanning and bean registration
- **Common Follow-up Questions**:
  1. What annotations inherit from `@Component`?
  2. When should you use `@Component` vs `@Bean`?
  3. How does Spring detect configuration beans?
- **Common Mistakes**: Using `@Component` for classes that require custom, parameter-based startup initializations.
- **Production Best Practice**: Avoid generic `@Component` annotations if `@Service` or `@Repository` fit the class's architectural role better.
- **Real Example from project**: [JwtAuthenticationFilter](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/security/JwtAuthenticationFilter.java) is annotated with `@Component` to register it in Spring's filter context.

---

### Q10: Explain Bean Lifecycle.
- **Why interviewer asks this**: To see if you can trace bean initialization and destruction phases within the ApplicationContext container.
- **Expected Interview Answer**: The lifecycle consists of: Instantiation (constructors), dependency injection, Aware interface execution, before-init post-processing, initialization (`@PostConstruct`), after-init post-processing (generates proxies), and destruction (`@PreDestroy` during context shutdown).
- **Simple Explanation**: How Spring creates, configures, initializes, and destroys Java objects.
- **Internal Working**: Custom post-processors intercept beans after dependency injection to wrap them in dynamic transaction proxies.
- **ASCII Flow Diagram**:
  ```
  [Instantiate] ──► [Inject Fields] ──► [PostProcessors] ──► [@PostConstruct] ──► [Proxy Wraps] ──► [@PreDestroy]
  ```
- **Related Project Classes**: [StripePaymentService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/StripePaymentService.java)
- **Related Technologies**: Spring BeanPostProcessor API
- **Common Follow-up Questions**:
  1. What is the role of BeanPostProcessor?
  2. How do you trigger custom cleanup methods?
  3. When is proxy creation executed?
- **Common Mistakes**: Expecting `@PostConstruct` to access fields initialized in post-process methods.
- **Production Best Practice**: Use bean lifecycle hooks to close resources and client connections cleanly during shutdown.
- **Real Example from project**: [StripePaymentService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/StripePaymentService.java) uses `@PostConstruct` to initialize Stripe's API key after dependency injection.

---

### Q11: Explain DispatcherServlet.
- **Why interviewer asks this**: To verify your understanding of Spring MVC's request routing architecture.
- **Expected Interview Answer**: `DispatcherServlet` is the front controller. It intercepts all incoming HTTP requests, maps them to controller methods using `HandlerMapping`, executes handlers using `HandlerAdapter`, and serializes returned objects into JSON payloads.
- **Simple Explanation**: The main gateway that receives web requests and routes them to the correct controller methods.
- **Internal Working**: It routes requests to controllers, runs validations, handles exceptions, and converts returned objects to JSON.
- **ASCII Flow Diagram**:
  ```
  HTTP Request ──► [DispatcherServlet] ──(HandlerMapping)──► [Controller] ──► JSON Response
  ```
- **Related Project Classes**: [AuthController](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/controller/AuthController.java)
- **Related Technologies**: Spring Web MVC, HttpMessageConverter
- **Common Follow-up Questions**:
  1. What is the role of HttpMessageConverter?
  2. How are validation exceptions caught?
  3. How does DispatcherServlet map request paths?
- **Common Mistakes**: Assuming `DispatcherServlet` processes security filters; security checks run before requests reach it.
- **Production Best Practice**: Customize Jackson parameters to filter null properties from JSON responses.
- **Real Example from project**: Routes authentication payloads to [AuthController](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/controller/AuthController.java) mappings.

---

### Q12: Why Constructor Injection?
- **Why interviewer asks this**: To evaluate your knowledge of object immutability and testing best practices.
- **Expected Interview Answer**: Constructor injection ensures dependencies are required, creates immutable objects, prevents circular dependencies during startup, and simplifies writing unit tests by allowing dependencies to be passed manually.
- **Simple Explanation**: Passing dependencies through constructor arguments to guarantee they are initialized and support testing.
- **Internal Working**: The JVM instantiates components, resolving constructor arguments and throwing circular reference errors immediately if cycles exist.
- **ASCII Flow Diagram**:
  ```
  Constructor Args ──► [JVM Instantiation] ──► Immutable Service Instance
  ```
- **Related Project Classes**: [FoodListingService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/FoodListingService.java)
- **Related Technologies**: Spring IoC Autowiring
- **Common Follow-up Questions**:
  1. How does constructor injection prevent circular references?
  2. What is the difference between constructor and field injection?
  3. Why make injection dependencies final?
- **Common Mistakes**: Using field injection (`@Autowired` on fields), which makes mocking dependencies difficult and hides circular references.
- **Production Best Practice**: Declare dependency fields as `final` to ensure immutability.
- **Real Example from project**: [FoodListingService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/FoodListingService.java) uses constructor injection (via Lombok's `@RequiredArgsConstructor`) to load repositories.

---

### Q13: Explain Spring Boot startup flow.
- **Why interviewer asks this**: To see if you can trace Spring Boot's startup steps from application launch to port exposure.
- **Expected Interview Answer**: The startup flow consists of:
  1. Initializing `SpringApplication`.
  2. Preparing the environment and property profiles.
  3. Creating the application context.
  4. Running classpath component scans.
  5. Instantiating beans and injecting dependencies.
  6. Starting the embedded Tomcat servlet container and exposing the configured port.
- **Simple Explanation**: How Spring Boot configures dependencies, initializes beans, and starts the server when launched.
- **Internal Working**: The JVM registers config settings, parses metadata, starts thread pools, and exposes the HTTP port.
- **ASCII Flow Diagram**:
  ```
  main() ──► [Config Properties] ──► [Bean Scan] ──► [Dependency Injection] ──► [Start Tomcat]
  ```
- **Related Project Classes**: [SurplusFoodMarketplaceApplication](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/SurplusFoodMarketplaceApplication.java)
- **Related Technologies**: SpringBootApplication configuration properties
- **Common Follow-up Questions**:
  1. How are active profiles loaded?
  2. What is context caching?
  3. When are initializers executed?
- **Common Mistakes**: Expecting database queries to execute before Spring completes bean initialization phases.
- **Production Best Practice**: Keep memory usage low during startup by lazy-loading heavy config items.
- **Real Example from project**: [SurplusFoodMarketplaceApplication](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/SurplusFoodMarketplaceApplication.java) runs `SpringApplication.run()` to start the server.

---

## Section 3 — Spring Security

### Q14: Explain complete JWT authentication flow.
- **Why interviewer asks this**: To verify your understanding of stateless API security implementations.
- **Expected Interview Answer**: The JWT authentication flow consists of:
  1. The client registers or logs in with their credentials.
  2. The server verifies the credentials and returns access and refresh token pairs.
  3. The client includes the JWT in the `Authorization: Bearer <token>` header of subsequent requests.
  4. The custom JWT filter intercepts requests, decodes and validates the token signature, and populates the security context.
- **Simple Explanation**: Generating tokens after login and verifying token signatures on subsequent requests to authenticate users.
- **Internal Working**: The filter verifies token signatures, decodes claims, loads user details, and sets the authenticated principal.
- **ASCII Flow Diagram**:
  ```
  Request (Bearer Token) ──► [JwtFilter] ──(Verify Sig)──► [SecurityContext] ──► [Controller]
  ```
- **Related Project Classes**: [JwtAuthenticationFilter](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/security/JwtAuthenticationFilter.java), [JwtService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/security/JwtService.java)
- **Related Technologies**: JSON Web Token, HMAC-SHA256, JWS
- **Common Follow-up Questions**:
  1. What is the structure of a JWT?
  2. How do you handle token expiration?
  3. How do you verify token signatures?
- **Common Mistakes**: Storing sensitive business data in JWT payloads; payloads are base64-encoded and can be read by anyone.
- **Production Best Practice**: Keep token lifetimes short (e.g., 15 minutes) and use refresh tokens for renewals.
- **Real Example from project**: [JwtAuthenticationFilter](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/security/JwtAuthenticationFilter.java) validates token signatures on protected API routes.

---

### Q15: Why JWT instead of Session?
- **Why interviewer asks this**: To evaluate your knowledge of stateful vs stateless architecture scaling trade-offs.
- **Expected Interview Answer**: Stateful sessions require storing session state in server memory or shared databases, which limits scaling. Stateless JWTs store user authentication state in signed tokens, allowing APIs to scale horizontally.
- **Simple Explanation**: Sessions require servers to store login states in memory; JWTs store login states in the token itself.
- **Internal Working**: The server verifies token signatures using its secret key, avoiding database lookups or session lookups.
- **ASCII Flow Diagram**:
  ```
  Stateless: Client Request (Signed JWT) ──► [Server validation using Secret Key] ──► Process
  ```
- **Related Project Classes**: [SecurityConfig](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/SecurityConfig.java)
- **Related Technologies**: Sessionless Security Configuration
- **Common Follow-up Questions**:
  1. How do you revoke compromised JWTs?
  2. What is the memory overhead of stateful sessions?
  3. What is the network overhead of large JWTs?
- **Common Mistakes**: Claiming JWTs are more secure than sessions; both must be protected from access attacks.
- **Production Best Practice**: Store tokens in secure, HttpOnly cookies in production to protect them from XSS attacks.
- **Real Example from project**: [SecurityConfig](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/SecurityConfig.java) disables sessions by setting `SessionCreationPolicy.STATELESS`.

---

### Q16: Explain Security Filter Chain.
- **Why interviewer asks this**: To see if you understand Spring Security's request processing pipeline.
- **Expected Interview Answer**: The servlet container routes requests to a `DelegatingFilterProxy`, which forwards them to a Spring-managed `FilterChainProxy`. This proxy routes requests through security filters to validate authentication, permissions, and CSRF settings.
- **Simple Explanation**: A chain of security filters that intercepts requests to handle tasks like authorization checks and header validations.
- **Internal Working**: Requests pass through security filters in sequence, returning error responses if checks fail.
- **ASCII Flow Diagram**:
  ```
  Tomcat request ──► [DelegatingFilterProxy] ──► [JwtFilter] ──► [AuthFilter] ──► API Route
  ```
- **Related Project Classes**: [SecurityConfig](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/SecurityConfig.java)
- **Related Technologies**: FilterChainProxy, OncePerRequestFilter
- **Common Follow-up Questions**:
  1. How do custom filters integrate into the chain?
  2. How do filters return error responses?
  3. What is the role of DelegatingFilterProxy?
- **Common Mistakes**: Expecting Spring MVC interceptors to execute before Spring Security filters.
- **Production Best Practice**: Place custom JWT validation filters before username-password filters to save resources.
- **Real Example from project**: [SecurityConfig](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/SecurityConfig.java) registers filters and configures endpoint authorizations.

---

### Q17: Explain JwtAuthenticationFilter.
- **Why interviewer asks this**: To evaluate your hands-on implementation details of servlet filter mechanics.
- **Expected Interview Answer**: Our custom filter extends `OncePerRequestFilter`. It reads the `Authorization: Bearer <token>` header, decodes and validates the token signature, loads user details, and populates Spring's `SecurityContextHolder`.
- **Simple Explanation**: A security filter that parses and validates token signatures on every protected request.
- **Internal Working**: Extracted credentials are cached in a `UsernamePasswordAuthenticationToken` and saved in the thread-local context.
- **ASCII Flow Diagram**:
  ```
  Request ──► [Read Header] ──► [Validate Token] ──► [Set SecurityContext] ──► doFilter()
  ```
- **Related Project Classes**: [JwtAuthenticationFilter](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/security/JwtAuthenticationFilter.java)
- **Related Technologies**: Spring Security Filter Pipeline
- **Common Follow-up Questions**:
  1. Why extend OncePerRequestFilter?
  2. How does the filter handle expired tokens?
  3. What happens if the Authorization header is missing?
- **Common Mistakes**: Running database queries in filters on every request, which degrades API performance.
- **Production Best Practice**: Fail fast inside filters to avoid processing invalid requests.
- **Real Example from project**: [JwtAuthenticationFilter](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/security/JwtAuthenticationFilter.java) parses requests, verifies tokens, and routes valid calls to endpoints.

---

### Q18: Explain SecurityContextHolder.
- **Why interviewer asks this**: To verify your understanding of how thread contexts store authenticated principal details.
- **Expected Interview Answer**: `SecurityContextHolder` stores security context details. It uses a `ThreadLocal` strategy under the hood to store authentication details, binding them to the current executing thread.
- **Simple Explanation**: A thread-specific store that holds the current authenticated user's details.
- **Internal Working**: The context remains bound to the current thread throughout its execution, and is cleared when Tomcat worker threads finish processing.
- **ASCII Flow Diagram**:
  ```
  Tomcat Thread ──► [ThreadLocal Map: SecurityContext] ──► Exposes Principal
  ```
- **Related Project Classes**: [JwtAuthenticationFilter](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/security/JwtAuthenticationFilter.java)
- **Related Technologies**: ThreadLocal, SecurityContext
- **Common Follow-up Questions**:
  1. How is thread safety achieved in SecurityContextHolder?
  2. When is the context cleared?
  3. What other context strategies exist?
- **Common Mistakes**: Modifying context contents manually in child threads without configuring context propagation.
- **Production Best Practice**: Ensure filter contexts clean up thread-local states to prevent memory leaks in thread pools.
- **Real Example from project**: [JwtAuthenticationFilter](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/security/JwtAuthenticationFilter.java) calls `SecurityContextHolder.getContext().setAuthentication(auth)` to cache user details.

---

### Q19: Explain BCrypt.
- **Why interviewer asks this**: To evaluate your knowledge of secure password hashing techniques.
- **Expected Interview Answer**: BCrypt is a slow hashing algorithm that uses a cost parameter to scale hashing complexity and appends a secure salt to protect against rainbow table attacks.
- **Simple Explanation**: A hashing algorithm that secures passwords using random salts to prevent decryption.
- **Internal Working**: It runs multiple hashing rounds based on the cost parameter, embedding the salt within the final hash string.
- **ASCII Flow Diagram**:
  ```
  Input Password + Random Salt ──► [Multiple Hashing Rounds] ──► Hashed Output
  ```
- **Related Project Classes**: [SecurityConfig](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/SecurityConfig.java), [AuthService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/AuthService.java)
- **Related Technologies**: BCrypt Hashing, PasswordEncoder
- **Common Follow-up Questions**:
  1. What is the role of the cost parameter?
  2. How does BCrypt verify passwords without storing salts separately?
  3. Why is BCrypt slow by design?
- **Common Mistakes**: Storing salts in separate database columns; BCrypt embeds the salt directly in the hash string.
- **Production Best Practice**: Set the cost parameter to balance security and password check speeds (e.g., 10-12).
- **Real Example from project**: [AuthService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/AuthService.java) uses `passwordEncoder.encode()` to hash passwords before database inserts.

---

### Q20: Explain CORS.
- **Why interviewer asks this**: To verify your understanding of cross-origin security rules and configuration options.
- **Expected Interview Answer**: CORS (Cross-Origin Resource Sharing) is a security mechanism that allows web browsers to restrict cross-origin HTTP requests. We configure allowed origins, methods, and headers to let our frontend access API endpoints safely.
- **Simple Explanation**: A browser security check that blocks requests from unauthorized domains.
- **Internal Working**: The browser sends a preflight `OPTIONS` request. The backend responds with CORS headers, and the browser sends the actual request only if domains match.
- **ASCII Flow Diagram**:
  ```
  Frontend (Host A) ──(OPTIONS)──► [Backend Host B] ──(Response Headers)──► Browser validation
  ```
- **Related Project Classes**: [SecurityConfig](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/SecurityConfig.java)
- **Related Technologies**: CORS Headers, Options Request
- **Common Follow-up Questions**:
  1. What is a preflight request?
  2. How do you configure CORS mappings?
  3. What headers are associated with CORS validations?
- **Common Mistakes**: Using wildcards (`*`) for allowed origins in production, which exposes APIs to unauthorized cross-origin requests.
- **Production Best Practice**: Configure strict CORS rules, whitelisting only the production frontend domains.
- **Real Example from project**: [SecurityConfig](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/SecurityConfig.java) registers a `CorsConfigurationSource` whitelisting specific origins.

---

### Q21: Explain CSRF.
- **Why interviewer asks this**: To evaluate your knowledge of Session-based vs Token-based request security threats.
- **Expected Interview Answer**: CSRF (Cross-Site Request Forgery) attacks exploit the browser's default behavior of automatically attaching session cookies to cross-site requests. Since stateless APIs pass JWTs in custom headers instead of cookies, browsers do not attach them automatically, making CSRF tokens redundant.
- **Simple Explanation**: An attack where a malicious site tricks the browser into sending requests to a site using stored login cookies.
- **Internal Working**: The browser does not attach authorization headers to cross-site requests automatically, protecting the stateless API from CSRF.
- **ASCII Flow Diagram**:
  ```
  Cross-Site Request (No JWT Header) ──► [Server checks Header] ──► Rejects request (401)
  ```
- **Related Project Classes**: [SecurityConfig](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/SecurityConfig.java)
- **Related Technologies**: Stateless Security configuration
- **Common Follow-up Questions**:
  1. How do cookies cause CSRF vulnerabilities?
  2. When is CSRF validation required?
  3. How does a CSRF token protect stateful APIs?
- **Common Mistakes**: Leaving CSRF enabled on stateless APIs, which requires client applications to handle redundant CSRF validation checks.
- **Production Best Practice**: Keep APIs stateless and store JWT tokens in secure HttpOnly cookies or request headers.
- **Real Example from project**: [SecurityConfig](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/SecurityConfig.java) disables CSRF protection using `.csrf(AbstractHttpConfigurer::disable)`.

---

## Section 4 — Hibernate & JPA

### Q22: Explain Entity Lifecycle.
- **Why interviewer asks this**: To see if you understand JPA state transitions and how Hibernate manages entity objects.
- **Expected Interview Answer**: Entities transition through four states:
  - **Transient**: Instantiated in memory but not mapped to database rows or tracked by a session.
  - **Managed**: Associated with an active session and database primary key. Modifications are tracked and saved automatically.
  - **Detached**: The session is closed; the entity exists in memory but changes are no longer monitored.
  - **Removed**: Marked for deletion; the corresponding record will be deleted from the database during the next session flush.
- **Simple Explanation**: The lifecycle states (transient, managed, detached, removed) an entity transitions through within Hibernate's database mapping context.
- **Internal Working**: JPA's `EntityManager` tracks managed entities, generating SQL INSERT, UPDATE, or DELETE queries during session flush cycles.
- **ASCII Flow Diagram**:
  ```
  Transient ──(persist)──► Managed ──(detach)──► Detached ──(merge)──► Managed
                              │
                          (remove)
                              ▼
                           Removed
  ```
- **Related Project Classes**: [User](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/User.java)
- **Related Technologies**: Hibernate EntityManager, Session States
- **Common Follow-up Questions**:
  1. How does merge() handle detached entities?
  2. What happens to entities when transactions commit?
  3. How do you detach managed entities?
- **Common Mistakes**: Expecting changes to detached entities to be saved to the database automatically.
- **Production Best Practice**: Understand entity states to avoid executing redundant repository update calls.
- **Real Example from project**: Modifying entity properties inside a transaction updates the database without calling `save()`.

---

### Q23: Why Lazy Loading?
- **Why interviewer asks this**: To verify your knowledge of performance tuning and database query optimizations.
- **Expected Interview Answer**: Lazy loading defers fetching related entities from the database until they are accessed in code. This reduces query sizes and memory usage compared to eager loading, which loads all associated records immediately.
- **Simple Explanation**: Loading child records from the database only when they are accessed in code, rather than loading them in the initial query.
- **Internal Working**: Hibernate injects lazy proxy wrappers for relationships, executing secondary queries only if child properties are accessed.
- **ASCII Flow Diagram**:
  ```
  Load Parent ──► Hibernate creates Lazy Proxy ──► Access Property ──► Secondary SQL query
  ```
- **Related Project Classes**: [FoodListing](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/FoodListing.java)
- **Related Technologies**: FetchType.LAZY, Hibernate Proxies
- **Common Follow-up Questions**:
  1. What is the difference between Lazy and Eager loading?
  2. How do you resolve a LazyInitializationException?
  3. How does Hibernate implement proxies?
- **Common Mistakes**: Mapping all entity relationships as EAGER by default, which can cause memory issues and slow queries.
- **Production Best Practice**: Use lazy loading by default, and retrieve related collections using fetch joins when needed.
- **Real Example from project**: [FoodListing](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/FoodListing.java) maps the `Business` association lazily to prevent loading seller profiles when listing items.

---

### Q24: Explain Cascade.
- **Why interviewer asks this**: To evaluate your understanding of cascading entity state updates down to related associations.
- **Expected Interview Answer**: Cascading propagates entity lifecycle state changes (like persist, merge, and remove) from the parent entity down to associated child entities, which helps automate record updates.
- **Simple Explanation**: Updating or deleting a parent record automatically updates or deletes associated child records.
- **Internal Working**: Hibernate scans mappings at runtime, executing child updates dynamically when parent entities change.
- **ASCII Flow Diagram**:
  ```
  Delete Parent ──(CascadeType.ALL)──► Deletes Associated Child Records automatically
  ```
- **Related Project Classes**: [FoodListing](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/FoodListing.java), [FoodListingImage](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/FoodListingImage.java)
- **Related Technologies**: CascadeType, orphanRemoval
- **Common Follow-up Questions**:
  1. What is the difference between CascadeType.REMOVE and orphanRemoval?
  2. What are the types of CascadeType?
  3. Why avoid CascadeType.ALL on many-to-many relationships?
- **Common Mistakes**: Using cascade remove on shared entities (like Category), which can delete shared records when other listings are removed.
- **Production Best Practice**: Apply cascade types selectively; avoid using generic settings like `ALL` unless necessary.
- **Real Example from project**: [FoodListing](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/FoodListing.java) maps child images using `cascade = CascadeType.ALL` and `orphanRemoval = true`.

---

### Q25: Explain @Transactional.
- **Why interviewer asks this**: To verify your understanding of database transaction boundaries and Spring's transaction management abstraction.
- **Expected Interview Answer**: `@Transactional` configures database transaction boundaries. It intercepts method execution using AOP proxies, borrows a connection from HikariCP, disables auto-commit, runs the logic, and commits changes on success or rolls back on runtime failures.
- **Simple Explanation**: An annotation that runs a method inside a database transaction, rolling back changes if errors occur.
- **Internal Working**: The transaction proxy intercepts execution, opens transactions, and commits changes or rolls back on runtime exceptions.
- **ASCII Flow Diagram**:
  ```
  Proxy intercept ──► Opens Connection ──► AutoCommit(false) ──► Commit/Rollback ──► Release Connection
  ```
- **Related Project Classes**: [AuthService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/AuthService.java)
- **Related Technologies**: Spring AOP Transaction Manager, JDBC Transactions
- **Common Follow-up Questions**:
  1. What happens if a transactional method calls another in the same class?
  2. How do checked exceptions affect transactions?
  3. Explain transaction isolation levels.
- **Common Mistakes**: Annotating private methods with `@Transactional`; Spring's AOP proxies intercept only public method calls.
- **Production Best Practice**: Keep transactions short and focused to avoid connection starvation under load.
- **Real Example from project**: [AuthService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/AuthService.java) uses `@Transactional` to ensure user registration and role assignments run within a single transaction.

---

### Q26: Explain Repository Pattern.
- **Why interviewer asks this**: To see if you understand data access abstraction layers and Spring Data JPA's repository implementation.
- **Expected Interview Answer**: The Repository Pattern isolates data access logic from business services. By extending JpaRepository, Spring automatically generates repository implementations at startup, resolving CRUD calls and method queries dynamically.
- **Simple Explanation**: Isolating database operations behind interfaces, letting the framework implement CRUD operations.
- **Internal Working**: Spring Data JPA generates proxy implementations of repository interfaces at startup to map database queries.
- **ASCII Flow Diagram**:
  ```
  Service ──► [Repository Interface Proxy] ──► [Spring Data JPA translation] ──► Database query
  ```
- **Related Project Classes**: [UserRepository](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/repository/UserRepository.java)
- **Related Technologies**: JpaRepository, Spring Data Common proxy mappings
- **Common Follow-up Questions**:
  1. How are method names resolved to SQL queries?
  2. What is the difference between JpaRepository and CrudRepository?
  3. When should you use custom repository implementations?
- **Common Mistakes**: Writing custom query logic manually for simple search operations that JpaRepository method names can resolve automatically.
- **Production Best Practice**: Paginating search results to prevent loading excessive records into memory.
- **Real Example from project**: [UserRepository](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/repository/UserRepository.java) extends `JpaRepository<User, Long>` to inherit standard database operations.

---

### Q27: Explain Dirty Checking.
- **Why interviewer asks this**: To verify your understanding of Hibernate's state management and automatic update mechanisms.
- **Expected Interview Answer**: Dirty checking is Hibernate's mechanism for tracking modifications to managed entities. During session flushes (usually right before transaction commits), Hibernate compares the entity's current state with its initial snapshot and automatically generates and executes SQL update queries for any modified fields.
- **Simple Explanation**: Modifying fields on loaded database records automatically generates update queries when the transaction commits, without you calling save().
- **Internal Working**: Hibernate compares the entity's current state with the cached snapshot, compiling SQL updates for modified properties during commit phases.
- **ASCII Flow Diagram**:
  ```
  Load record ──► Create Snapshot ──► Modify fields ──► Compare states on Flush ──► SQL Update
  ```
- **Related Project Classes**: [FoodListingService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/FoodListingService.java)
- **Related Technologies**: Hibernate Session Flush, Snapshot comparison
- **Common Follow-up Questions**:
  1. How does dirty checking affect performance?
  2. When does Hibernate run the flush operation?
  3. How can you disable dirty checking for read-only queries?
- **Common Mistakes**: Executing redundant `repository.save()` calls on managed entities inside transactional services.
- **Production Best Practice**: Mark read-only queries as `@Transactional(readOnly = true)` to disable snapshot caching and optimize performance.
- **Real Example from project**: Updating listing status flags inside service transactions triggers updates automatically on commit.

---

### Q28: Explain Persistence Context.
- **Why interviewer asks this**: To evaluate your knowledge of Hibernate's internal L1 caching and session-scoped entity tracking.
- **Expected Interview Answer**: The persistence context is a session-scoped cache (L1 cache) that tracks entity states. It prevents duplicate database lookups by caching loaded records. If an object is retrieved multiple times within a single transaction, Hibernate serves it from memory.
- **Simple Explanation**: A temporary cache that tracks database entities loaded during a transaction to prevent duplicate queries.
- **Internal Working**: The L1 cache maps entities by ID, serving them from memory on duplicate requests and flushing updates to the database on commit.
- **ASCII Flow Diagram**:
  ```
  Query ID ──► [L1 Cache Check] ──┬──► (Hit) ──► Return entity from memory
                                  └──► (Miss) ──► Query DB ──► Cache entity ──► Return
  ```
- **Related Project Classes**: [User](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/User.java)
- **Related Technologies**: L1 Cache, Session Context
- **Common Follow-up Questions**:
  1. What is the lifecycle of the L1 cache?
  2. How do you clear the persistence context manually?
  3. What is the difference between L1 cache and L2 cache?
- **Common Mistakes**: Assuming the L1 cache is shared across threads; it is scoped to the active session and is not thread-safe.
- **Production Best Practice**: Clear the context session when processing large datasets in batch operations to prevent memory leaks.
- **Real Example from project**: Querying the authenticated user multiple times within a single request context retrieves the cached entity.

---

### Q29: Explain Native Query.
- **Why interviewer asks this**: To see when and why you choose native SQL queries over JPQL or object mappings.
- **Expected Interview Answer**: Native queries execute raw SQL directly against the database, bypassing Hibernate's JPQL parser. We use them to run coordinate-based spatial queries implementing the Haversine formula, which standard JPQL does not support.
- **Simple Explanation**: Executing raw SQL statements directly in the database instead of using Java object queries.
- **Internal Working**: The database engine runs the query directly, bypassing Hibernate's class mapping layers and returning raw results.
- **ASCII Flow Diagram**:
  ```
  Repository Method ──► [Raw SQL query] ──► MySQL Engine spatial execution ──► Result set
  ```
- **Related Project Classes**: [UserRepository](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/repository/UserRepository.java)
- **Related Technologies**: nativeQuery = true, Haversine formula
- **Common Follow-up Questions**:
  1. What are the disadvantages of native queries?
  2. How do you map native query results to custom DTOs?
  3. How does JPQL handle database dialec differences?
- **Common Mistakes**: Hardcoding database-specific SQL queries when standard JPQL queries can resolve the lookup, which reduces database portability.
- **Production Best Practice**: Apply indexing to columns queried by native SQL searches to optimize query performance.
- **Real Example from project**: [UserRepository](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/repository/UserRepository.java) uses a native query running the Haversine formula to search for nearby consumers and NGOs.

---

## Section 5 — Database

### Q30: Explain database design.
- **Why interviewer asks this**: To evaluate your schema modeling skills and relational design patterns.
- **Expected Interview Answer**: The schema design uses relational database tables to track entities (Users, Roles, Listings, Orders, Payments, Wishlists). Relationships are modeled using primary and foreign key constraints, and geolocations are indexed spatially.
- **Simple Explanation**: Structuring database tables and columns using relationships and indexes to ensure data integrity and query performance.
- **Internal Working**: Tables are configured in MySQL's InnoDB engine, enforcing relational integrity rules and using B-Tree indexes.
- **ASCII Flow Diagram**:
  ```
  [users] ──(1-to-Many)──► [businesses] ──(1-to-Many)──► [food_listings]
  ```
- **Related Project Classes**: [schema.sql](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/resources/schema.sql)
- **Related Technologies**: MySQL InnoDB Storage Engine, SQL Schemas
- **Common Follow-up Questions**:
  1. What normalization level is your schema built on?
  2. Why use MySQL InnoDB over other engines?
  3. How are spatial columns configured in MySQL?
- **Common Mistakes**: Storing coordinate values as plain text strings instead of numeric decimals, which prevents distance calculations.
- **Production Best Practice**: Configure database schemas using version-controlled migration scripts (like Flyway).
- **Real Example from project**: Mapped database tables and constraints are defined in [schema.sql](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/resources/schema.sql).

---

### Q31: Explain relationships.
- **Why interviewer asks this**: To see how you map database relationships (One-to-Many, Many-to-Many) in JPA and relational schemas.
- **Expected Interview Answer**: Relationships are mapped using foreign keys in tables and JPA annotations in classes: Users and Roles are mapped as many-to-many using a join table, and Listings are mapped as many-to-one to Businesses.
- **Simple Explanation**: Defining how database tables link together using keys and intermediate join tables.
- **Internal Working**: Hibernate joins tables at runtime using mapping metadata, executing join queries to load related entities.
- **ASCII Flow Diagram**:
  ```
  [users] (PK: id) ◄───(Foreign Key: business_id)─── [food_listings]
  ```
- **Related Project Classes**: [User](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/User.java), [FoodListing](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/FoodListing.java)
- **Related Technologies**: Many-to-One, Many-to-Many, Join Table
- **Common Follow-up Questions**:
  1. What is the mappedBy attribute used for?
  2. How do join tables map many-to-many relationships?
  3. How do you prevent recursive JSON serialization in relationships?
- **Common Mistakes**: Using EAGER fetching on relationships, which can lead to large database queries and memory issues.
- **Production Best Practice**: Prefer lazy fetching for relationships to keep initial database queries lightweight.
- **Real Example from project**: [User](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/User.java) defines a many-to-many relationship with roles using the join table `user_roles`.

---

### Q32: Explain indexes.
- **Why interviewer asks this**: To verify your understanding of database search performance and query execution speeds.
- **Expected Interview Answer**: Indexes speed up search lookups by creating searchable sorted data structures. We define a unique index on user emails to speed up logins, and spatial indexes on coordinate columns to optimize radius searches.
- **Simple Explanation**: Creating searchable sorted lists of column values to help the database find records quickly without scanning the whole table.
- **Internal Working**: MySQL InnoDB stores table indexes as B-Trees, allowing search queries to resolve keys in logarithmic time.
- **ASCII Flow Diagram**:
  ```
  Search email ──► [B-Tree Index Lookup] ──► Resolve primary key pointer ──► Load row
  ```
- **Related Project Classes**: [schema.sql](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/resources/schema.sql)
- **Related Technologies**: B-Tree Indexes, Spatial Indexing
- **Common Follow-up Questions**:
  1. How do indexes impact database write performance?
  2. What is a composite index?
  3. How do you verify if a query uses an index?
- **Common Mistakes**: Creating too many indexes on high-write tables, which degrades write and update performance.
- **Production Best Practice**: Create indexes only on columns queried frequently in WHERE clauses or joins.
- **Real Example from project**: [schema.sql](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/resources/schema.sql) defines a unique index on user emails and spatial indexes on coordinate columns.

---

### Q33: Explain transactions.
- **Why interviewer asks this**: To evaluate your knowledge of database locking behaviors, isolation levels, and data integrity.
- **Expected Interview Answer**: Transactions group database updates to execute them as a single logical unit. MySQL InnoDB uses row-level locking to support transactional isolation levels (like `REPEATABLE READ`), ensuring concurrent changes do not corrupt data.
- **Simple Explanation**: A set of database updates that must all succeed together, or all be rolled back to protect data consistency.
- **Internal Working**: The database engine writes changes to transactional undo/redo logs, locking affected rows until changes commit.
- **ASCII Flow Diagram**:
  ```
  Begin Transaction ──► Apply changes & lock rows ──► Commit logs ──► Release locks
  ```
- **Related Project Classes**: [AuthService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/AuthService.java)
- **Related Technologies**: ACID properties, Row-Level Locking, Undo Log
- **Common Follow-up Questions**:
  1. What is a transaction deadlock, and how is it resolved?
  2. Explain the difference between optimistic and pessimistic locking.
  3. What are transaction undo and redo logs?
- **Common Mistakes**: Keeping transactions open during slow, external API calls, which can block database connections.
- **Production Best Practice**: Keep transactions short and focused to avoid connection starvation under load.
- **Real Example from project**: Creating an order modifies listing quantities and records transaction logs within a single database transaction.

---

### Q34: Explain database optimization.
- **Why interviewer asks this**: To see how you debug, analyze, and optimize database query bottlenecks in production.
- **Expected Interview Answer**: Optimization involves checking execution plans (using `EXPLAIN`), defining indexes on query columns, avoiding N+1 queries using fetch joins, and configuring connection pool limits.
- **Simple Explanation**: Optimizing SQL queries and database configurations to speed up data retrieval and updates.
- **Internal Working**: Database engines analyze index allocations and join strategies to build and run query execution plans.
- **ASCII Flow Diagram**:
  ```
  Slow SQL query ──► [EXPLAIN analysis] ──► Add missing indexes ──► Optimized execution
  ```
- **Related Project Classes**: [UserRepository](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/repository/UserRepository.java)
- **Related Technologies**: EXPLAIN execution plan, Query Optimizer
- **Common Follow-up Questions**:
  1. What does the rows parameter in an EXPLAIN plan show?
  2. How does index coverage optimize queries?
  3. How does connection pooling improve performance?
- **Common Mistakes**: Executing queries without verification, leading to full table scans and performance degradation under load.
- **Production Best Practice**: Enable slow query logs to identify and optimize slow queries.
- **Real Example from project**: [UserRepository](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/repository/UserRepository.java) spatial searches are optimized using spatial indexes to prevent full table scans.

---

## Section 6 — Project Features

### Q35: Explain Food Listing flow.
- **Why interviewer asks this**: To evaluate your ability to trace data flows across layers for core business features.
- **Expected Interview Answer**: A validated seller requests listing creation. The controller validates parameters and calls the service. The service verifies the seller's account status, saves the listing to the database, and publishes a creation event to broadcast updates.
- **Simple Explanation**: The workflow of creating, validating, persisting, and broadcasting new surplus food listings.
- **Internal Working**: The service verifies the business status, maps DTOs to entities, persists the record, and broadcasts the creation event.
- **ASCII Flow Diagram**:
  ```
  Request DTO ──► [Controller] ──► [Verify Business] ──► [Save Listing] ──► Publish event
  ```
- **Related Project Classes**: [FoodListingController](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/controller/FoodListingController.java), [FoodListingService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/FoodListingService.java)
- **Related Technologies**: Spring Data JPA, Hibernate, JSR-380 Validation
- **Common Follow-up Questions**:
  1. What validation annotations are applied to listings?
  2. How do you verify business verification status?
  3. How do you query nearby listings?
- **Common Mistakes**: Allowing unverified businesses to post listings by omitting validation checks.
- **Production Best Practice**: Set validation bounds on listing expiration times to prevent invalid dates.
- **Real Example from project**: [FoodListingService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/FoodListingService.java) verifies seller status using `business.isVerified()` before saving new listings.

---

### Q36: Explain Notification flow.
- **Why interviewer asks this**: To see if you understand event-driven system designs and asynchronous updates.
- **Expected Interview Answer**: When a listing is created, the service publishes an internal event. An asynchronous listener catches this event after transaction commit, identifies nearby users, writes notifications to the database, and broadcasts them via WebSockets.
- **Simple Explanation**: Decoupling the notification flow by publishing events and processing alerts in background threads after database saves.
- **Internal Working**: The listener captures events, queries nearby users, persists notification records, and broadcasts updates over WebSockets.
- **ASCII Flow Diagram**:
  ```
  Commit transaction ──► [Async Listener] ──► Identify Users ──► Save Notification ──► WS Broadcast
  ```
- **Related Project Classes**: [TransactionEventListener](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/listener/TransactionEventListener.java), [NotificationService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/NotificationService.java)
- **Related Technologies**: Spring Application Events, WebSockets, STOMP
- **Common Follow-up Questions**:
  1. How are notifications stored in the database?
  2. Why process notifications asynchronously?
  3. How are WebSocket channels targeted to specific users?
- **Common Mistakes**: Processing notifications synchronously, which blocks HTTP threads and slows down listing creation.
- **Production Best Practice**: Set up background task execution limits to prevent thread pool exhaustion under heavy traffic.
- **Real Example from project**: [TransactionEventListener](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/listener/TransactionEventListener.java) triggers notification saves and WebSocket broadcasts when listings commit.

---

### Q37: Explain Event Driven Architecture.
- **Why interviewer asks this**: To evaluate your knowledge of asynchronous messaging patterns and module decoupling.
- **Expected Interview Answer**: Event-Driven Architecture decouples services by publishing domain events using Spring's event bus. Decoupled listeners capture these events and execute background tasks (like notifications or emails) without blocking core operations.
- **Simple Explanation**: Decoupling services by publishing events and processing background tasks asynchronously, keeping core logic focused.
- **Internal Working**: Services publish events using `ApplicationEventPublisher`. The event multicaster resolves listeners and delegates task execution to background thread pools.
- **ASCII Flow Diagram**:
  ```
  Service ──► [ApplicationEventPublisher] ──► Event Bus ──► [Async Listeners]
  ```
- **Related Project Classes**: [FoodListingService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/FoodListingService.java), [TransactionEventListener](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/listener/TransactionEventListener.java)
- **Related Technologies**: ApplicationEventPublisher, SimpleApplicationEventMulticaster
- **Common Follow-up Questions**:
  1. What is the default thread execution behavior for events?
  2. How do you configure asynchronous events?
  3. How do you handle listener execution errors?
- **Common Mistakes**: Assuming events are asynchronous by default; Spring runs events synchronously unless configure otherwise.
- **Production Best Practice**: Set up dedicated thread pools for event listeners to isolate them from request threads.
- **Real Example from project**: [FoodListingService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/FoodListingService.java) publishes a `FoodListingCreatedEvent` when listings are created.

---

### Q38: How does @TransactionalEventListener ensure post-commit execution?
- **Why interviewer asks this**: To verify your understanding of database transaction synchronization and lifecycle event hooks.
- **Expected Interview Answer**: `@TransactionalEventListener` registers execution hooks with the transaction manager. Setting the phase to `AFTER_COMMIT` ensures the listener runs only *after* the database transaction commits, preventing notifications if updates fail.
- **Simple Explanation**: An event listener configuration that executes code only after database transactions commit successfully.
- **Internal Working**: The transaction manager registers the listener, executing it during the transaction completion phase after committing updates.
- **ASCII Flow Diagram**:
  ```
  Write updates ──► Commit transaction ──► [Trigger Transactional Listener] ──► Run task
  ```
- **Related Project Classes**: [TransactionEventListener](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/listener/TransactionEventListener.java)
- **Related Technologies**: TransactionSynchronizationManager, TransactionPhase
- **Common Follow-up Questions**:
  1. What happens if the database transaction rolls back?
  2. What are the other transaction phases supported?
  3. Can you write to the database inside an AFTER_COMMIT listener?
- **Common Mistakes**: Expecting database writes to commit inside an `AFTER_COMMIT` listener without opening a new transaction.
- **Production Best Practice**: Configure listeners to execute asynchronously to avoid holding onto transaction threads.
- **Real Example from project**: [TransactionEventListener](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/listener/TransactionEventListener.java) listens for listing events and runs after database transactions commit.

---

### Q39: How does @Async delegate tasks to background thread pools?
- **Why interviewer asks this**: To evaluate your knowledge of asynchronous processing, threading APIs, and Spring task executors.
- **Expected Interview Answer**: `@Async` delegates method execution to a task executor. Spring intercepts the call using AOP proxies, submits the task to a background thread pool, and returns execution context immediately.
- **Simple Explanation**: Running heavy tasks in background threads to avoid blocking client request threads.
- **Internal Working**: The proxy submits tasks to an `Executor` pool, executing the method asynchronously.
- **ASCII Flow Diagram**:
  ```
  Method call ──► [AOP Proxy Interceptor] ──► Submit task to Thread Pool ──► Return context
  ```
- **Related Project Classes**: [TransactionEventListener](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/listener/TransactionEventListener.java), [NotificationScheduler](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/NotificationScheduler.java)
- **Related Technologies**: ThreadPoolTaskExecutor, TaskExecutor
- **Common Follow-up Questions**:
  1. What is the default task executor used by Spring?
  2. How do you configure a custom thread pool for async tasks?
  3. How do async methods return values?
- **Common Mistakes**: Calling `@Async` methods from within the same class, which bypasses the proxy and runs the method synchronously.
- **Production Best Practice**: Configure bounded thread pools to prevent resource exhaustion under heavy traffic.
- **Real Example from project**: [TransactionEventListener](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/listener/TransactionEventListener.java) uses `@Async` to send notifications and emails in background threads.

---

### Q40: Explain WebSocket protocol upgrade and backend STOMP configuration.
- **Why interviewer asks this**: To see if you understand full-duplex network communication channels and STOMP message routing.
- **Expected Interview Answer**: WebSockets establish bi-directional communication channels. The client upgrades standard HTTP connections to TCP connections. We use the STOMP protocol to route messages to destinations like `/topic` or `/queue` using an in-memory broker.
- **Simple Explanation**: Upgrading connections to keep full-duplex communication channels open, routing messages using topics.
- **Internal Working**: The server upgrades connections, registers subscriptions, and routes messages using target channels.
- **ASCII Flow Diagram**:
  ```
  HTTP Request (Upgrade Header) ──► [TCP Handshake Upgrade] ──► Persistent STOMP Channel
  ```
- **Related Project Classes**: [WebSocketConfig](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/WebSocketConfig.java)
- **Related Technologies**: WebSocket Protocol, STOMP, Message Broker
- **Common Follow-up Questions**:
  1. What is the purpose of heartbeat settings in WebSockets?
  2. How do you authorize WebSocket connections?
  3. What is the difference between WebSockets and polling?
- **Common Mistakes**: Forgetting to configure WebSocket origins, which can block connections from client applications.
- **Production Best Practice**: Set up heartbeats to detect and close dead TCP connections, freeing server resources.
- **Real Example from project**: [WebSocketConfig](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/config/WebSocketConfig.java) configures the STOMP message broker registry and endpoint mappings.

---

### Q41: Explain Stripe payment intent checkout flow.
- **Why interviewer asks this**: To evaluate your payment integration workflows and checkout process design.
- **Expected Interview Answer**: The checkout flow consists of:
  1. The client requests checkout.
  2. The backend validates quantities, converts prices to cents, and calls Stripe to create a `PaymentIntent`.
  3. Stripe returns a `client_secret` identifier.
  4. The client uses Stripe elements to submit payment details directly to Stripe.
- **Simple Explanation**: Initializing checkouts with Stripe to get a payment secret, letting clients pay Stripe directly.
- **Internal Working**: The backend creates payment records with `REQUIRES_PAYMENT_METHOD` status and returns the client secret.
- **ASCII Flow Diagram**:
  ```
  Checkout Request ──► [Create PaymentIntent] ──► Get Client Secret ──► Client pays Stripe
  ```
- **Related Project Classes**: [PaymentController](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/controller/PaymentController.java), [StripePaymentService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/StripePaymentService.java)
- **Related Technologies**: Stripe API, PaymentIntent
- **Common Follow-up Questions**:
  1. Why convert currency amounts to cents?
  2. How is order validation handled during checkout?
  3. What is the client secret used for?
- **Common Mistakes**: Storing transaction amounts as floats or doubles, which can introduce rounding errors.
- **Production Best Practice**: Lock inventory items during checkout to prevent double-booking.
- **Real Example from project**: [StripePaymentService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/StripePaymentService.java) calls Stripe's SDK to create a `PaymentIntent` for the order amount.

---

### Q42: How does the Payment Controller verify and process Stripe webhook events?
- **Why interviewer asks this**: To evaluate your security practices for external integrations and webhook data processing.
- **Expected Interview Answer**: The controller intercepts Stripe webhook notifications, validates the payload signature using the webhook secret, maps events (like `payment_intent.succeeded`), updates order statuses, and schedules pickups.
- **Simple Explanation**: Verifying that payment notifications came from Stripe before updating database orders.
- **Internal Working**: The SDK validates signatures using the payload and secret, updating order and payment records on success.
- **ASCII Flow Diagram**:
  ```
  Stripe Notification ──► [Validate Signature] ──► payment_intent.succeeded ──► Update Order
  ```
- **Related Project Classes**: [PaymentController](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/controller/PaymentController.java), [StripePaymentService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/StripePaymentService.java)
- **Related Technologies**: Stripe Webhook Signature, Event Mapper
- **Common Follow-up Questions**:
  1. How do you prevent duplicate webhook processing?
  2. What happens if webhook verification fails?
  3. How do you handle failed payment webhooks?
- **Common Mistakes**: Processing webhook updates without validating signatures, exposing endpoints to fake notifications.
- **Production Best Practice**: Make webhook handlers idempotent by checking database transaction statuses before updating records.
- **Real Example from project**: [StripePaymentService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/StripePaymentService.java) verifies webhook payloads using `Webhook.Signature.verifyHeader()`.

---

### Q43: Explain Cloudinary media upload and database metadata mapping.
- **Why interviewer asks this**: To evaluate your knowledge of media storage designs and database optimization strategies.
- **Expected Interview Answer**: Storing images directly in databases causes performance issues. Instead, the frontend uploads files directly to Cloudinary. The client includes the resulting image URLs and public IDs in listing payloads, which the backend stores in the database.
- **Simple Explanation**: Saving image URLs and public IDs in the database instead of storing raw files to optimize performance.
- **Internal Working**: The repository maps image URLs as entity properties, linking them to food listings.
- **ASCII Flow Diagram**:
  ```
  Upload image ──► [Cloudinary] ──► Get URL ──► Post Listing (URL) ──► [Database]
  ```
- **Related Project Classes**: [FoodListing](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/FoodListing.java), [FoodListingImage](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/FoodListingImage.java)
- **Related Technologies**: Cloudinary API, Image metadata mappings
- **Common Follow-up Questions**:
  1. How do you delete images from Cloudinary when listings are removed?
  2. Why offload image uploads to the client?
  3. How are image files represented in entity models?
- **Common Mistakes**: Storing images as byte arrays in SQL databases, which consumes heap memory and degrades performance.
- **Production Best Practice**: Clean up unused images from cloud storage asynchronously to save space.
- **Real Example from project**: [FoodListingImage](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/FoodListingImage.java) stores Cloudinary URLs and public IDs in the database.

---

### Q44: Explain Global Exception Handling architecture and input validations.
- **Why interviewer asks this**: To see if you write robust APIs that handle input validations and errors cleanly.
- **Expected Interview Answer**: We use a centralized handler annotated with `@RestControllerAdvice`. It intercepts unhandled exceptions (like input validation failures), extracts error details, and returns structured JSON responses with correct HTTP status codes.
- **Simple Explanation**: A centralized error handler that catches API exceptions and returns formatted JSON errors to clients.
- **Internal Working**: Spring MVC catches exception events and routes them to handler methods, formatting error details into standard responses.
- **ASCII Flow Diagram**:
  ```
  Validation Failure ──► [GlobalExceptionHandler] ──► Format field errors ──► HTTP 400 JSON
  ```
- **Related Project Classes**: [GlobalExceptionHandler](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/exception/GlobalExceptionHandler.java)
- **Related Technologies**: RestControllerAdvice, ExceptionHandler, BindingResult
- **Common Follow-up Questions**:
  1. How are validation constraints (like @Min) declared on DTOs?
  2. How do you catch custom runtime exceptions?
  3. What HTTP status code is returned for validation failures?
- **Common Mistakes**: Returning raw stack traces to client applications, which is a security risk.
- **Production Best Practice**: Return standard JSON error formats (timestamp, path, errors list) to help client integration.
- **Real Example from project**: [GlobalExceptionHandler](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/exception/GlobalExceptionHandler.java) catches `MethodArgumentNotValidException` to return field validation errors.

---

## Section 7 — Production & Scalability

### Q45: How would you scale the database to support 10 million active users?
- **Why interviewer asks this**: To evaluate your knowledge of system design, database scaling, and distributed architecture patterns.
- **Expected Interview Answer**: I would scale the database using **Read Replicas** to route read queries away from the master node, **Caching (Redis)** for frequently read data, and **Database Sharding** (e.g., by location/zipcode) to partition tables across servers.
- **Simple Explanation**: Scaling database performance by using caches, splitting tables across servers, and routing reads to replica instances.
- **Internal Working**: Read replicas sync data from the master node. Master nodes handle writes, while replica nodes process read queries.
- **ASCII Flow Diagram**:
  ```
  Write Traffic ──► [Master DB] ──(Replication)──► [Read Replicas] ◄── Read Traffic
  ```
- **Related Project Classes**: [UserRepository](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/repository/UserRepository.java)
- **Related Technologies**: Read/Write Splitting, MySQL Replication
- **Common Follow-up Questions**:
  1. How do you manage replication lag?
  2. What columns would you shard database tables on?
  3. How does connection pooling change in distributed database setups?
- **Common Mistakes**: Assuming NoSQL is always the solution for scaling relational databases; SQL databases can scale using replicas and partitioning.
- **Production Best Practice**: Set up database monitoring alerts to track replication lag and query load.
- **Real Example from project**: Spatial coordinate queries can be offloaded to read replicas to optimize database performance.

---

### Q46: What is your database failover and connection timeout strategy?
- **Why interviewer asks this**: To evaluate your database resilience, connection pooling, and fault tolerance configurations.
- **Expected Interview Answer**: I configure database failover using master-slave replication with automated failover utilities (like Orchestrator). In HikariCP, I tune connection, leak detection, and idle timeouts to prevent connection pool starvation.
- **Simple Explanation**: Setting connection timeouts and automated failover rules to ensure the application remains stable if the database goes down.
- **Internal Working**: HikariCP monitors connection health, closing dead connections and routing traffic to new master instances during failovers.
- **ASCII Flow Diagram**:
  ```
  Master Down ──► [Failover Manager] ──► Promote Replica to Master ──► HikariCP reconnects
  ```
- **Related Project Classes**: [application.yml](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/resources/application.yml)
- **Related Technologies**: HikariCP Connection Pool, Automated Failover
- **Common Follow-up Questions**:
  1. What is the role of HikariCP's connection-timeout property?
  2. How do you detect connection leaks?
  3. What is the difference between active and passive failover?
- **Common Mistakes**: Setting connection timeouts too high, which can cause thread pools to block when databases are slow.
- **Production Best Practice**: Enable connection leak detection in HikariCP to track unclosed database connections.
- **Real Example from project**: [application.yml](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/resources/application.yml) configures HikariCP connection properties.

---

### Q47: How do you ensure payment consistency if the Stripe webhook delivery fails?
- **Why interviewer asks this**: To see if you write resilient code that handles integration failures and data consistency issues.
- **Expected Interview Answer**: I implement two safeguards:
  1. A scheduled job in the background checks for pending orders and queries Stripe's API to verify transaction statuses.
  2. Webhook endpoints are made idempotent by checking order statuses before processing updates.
- **Simple Explanation**: Running background checks and verifying statuses to ensure order details sync if notifications are missed.
- **Internal Working**: The background job queries Stripe's API for transaction details, updating payment records accordingly.
- **ASCII Flow Diagram**:
  ```
  Stripe Webhook Failed ──► [Scheduled Sync Job] ──► Query Stripe API ──► Update Order Status
  ```
- **Related Project Classes**: [StripePaymentService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/StripePaymentService.java), [NotificationScheduler](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/resources/application.yml)
- **Related Technologies**: Stripe API Query, Scheduled Tasks
- **Common Follow-up Questions**:
  1. How do you make webhook handlers idempotent?
  2. What is the transaction synchronization logic used?
  3. How often should the sync job run?
- **Common Mistakes**: Relying only on webhooks for payment updates, which can cause order mismatches if notifications fail.
- **Production Best Practice**: Log all webhook payloads and verify signatures to track notification health.
- **Real Example from project**: [StripePaymentService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/StripePaymentService.java) verifies payment statuses when updating checkout records.

---

### Q48: What is your token refresh rotation strategy if access tokens expire?
- **Why interviewer asks this**: To evaluate your knowledge of secure session management, JWT expiration, and token reissue policies.
- **Expected Interview Answer**: When access tokens expire, the client requests renewals using a refresh token. We store refresh tokens in the database, verify them against expiration bounds, rotate them on reuse, and revoke active tokens if theft is suspected.
- **Simple Explanation**: Using long-lived refresh tokens to request new short-lived access tokens, rotating them on use to prevent unauthorized reuse.
- **Internal Working**: The refresh service validates tokens, generates a new access-refresh token pair, and revokes the old pair.
- **ASCII Flow Diagram**:
  ```
  Access Expired ──► Request with Refresh Token ──► [Rotate Token Pair] ──► Return new access token
  ```
- **Related Project Classes**: [RefreshToken](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/entity/RefreshToken.java), [RefreshTokenService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/RefreshTokenService.java)
- **Related Technologies**: Refresh Token Rotation (RTR), JWT Lifecycle
- **Common Follow-up Questions**:
  1. Why store refresh tokens in the database if JWT is stateless?
  2. How do you detect refresh token theft?
  3. What is the lifetime of a refresh token?
- **Common Mistakes**: Storing refresh tokens in cookies without HttpOnly flags, making them vulnerable to access attacks.
- **Production Best Practice**: Implement refresh token rotation to detect and prevent unauthorized token reuse.
- **Real Example from project**: [RefreshTokenService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/RefreshTokenService.java) manages refresh token lifecycles and validation checks.

---

### Q49: How would you cache food listings in Redis, and how do you prevent cache stampedes?
- **Why interviewer asks this**: To evaluate your knowledge of caching, Redis integration, and cache stampede prevention techniques.
- **Expected Interview Answer**: I would cache active listings in Redis using a cache-aside pattern. To prevent cache stampedes, I would configure locks to serialize queries when keys expire, or schedule background tasks to update keys before they expire.
- **Simple Explanation**: Storing hot catalog data in Redis to optimize reads, using locks to prevent database overload when keys expire.
- **Internal Working**: The service checks Redis first. If it's a miss, it acquires a lock, queries the database, updates the cache, and releases the lock.
- **ASCII Flow Diagram**:
  ```
  Cache Miss ──► [Acquire Lock] ──► Query Database ──► Update Redis Cache ──► Release Lock
  ```
- **Related Project Classes**: [FoodListingService](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/service/FoodListingService.java)
- **Related Technologies**: Redis Cache, Cache Stampede, Distributed Locks
- **Common Follow-up Questions**:
  1. What is cache penetration, and how do you prevent it?
  2. What is cache breakdown?
  3. How do you configure key expiration (TTL) policies?
- **Common Mistakes**: Caching datasets without configuring key expiration policies, which can exhaust Redis memory.
- **Production Best Practice**: Set up randomized key expiration times (jitter) to prevent all keys from expiring at the same time.
- **Real Example from project**: Category lists can be cached in Redis with long expiration times to optimize lookup queries.

---

### Q50: How would you improve overall API performance and reduce latency?
- **Why interviewer asks this**: To evaluate your backend optimization, caching, and performance diagnostics experience.
- **Expected Interview Answer**: I would optimize performance by using Redis to cache query results, indexing database search columns, configuring HTTP/2 to enable request multiplexing, using MapStruct compile-time mapping, and tuning HikariCP pool sizes.
- **Simple Explanation**: Speeding up APIs by using caching, indexing database columns, optimizing JVM settings, and recycling database connections.
- **Internal Working**: Optimizations reduce database queries, compile mappings at compile time, and reuse open database connections.
- **ASCII Flow Diagram**:
  ```
  Inbound Request ──► [Redis Cache Check] ──► (Hit) ──► Return response instantly
  ```
- **Related Project Classes**: [FoodListingMapper](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/mapper/FoodListingMapper.java)
- **Related Technologies**: HTTP/2, HikariCP, MapStruct compile-time mappings
- **Common Follow-up Questions**:
  1. How does HTTP/2 multiplexing work?
  2. What is compile-time object mapping?
  3. How do database indexes reduce API latency?
- **Common Mistakes**: Optimizing code paths without profiling first, which can waste development effort.
- **Production Best Practice**: Set up profiling tools (like JProfiler) in test environments to identify performance bottlenecks.
- **Real Example from project**: [FoodListingMapper](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/java/com/surplusfood/marketplace/mapper/FoodListingMapper.java) maps entities to DTOs at compile time, avoiding reflection overhead.

---

### Q51: What is your logging profile structure, and how do you configure rotation?
- **Why interviewer asks this**: To evaluate your production monitoring, logging configs, and debugging setups.
- **Expected Interview Answer**: I configure logging using SLF4J and Logback. Logs are split by profiles (e.g., Console for Dev, Rolling File for Prod) and structured in JSON format. Rotation policies rotate files based on size and date to manage disk space.
- **Simple Explanation**: Configuring logging levels and rotation policies to save search history without filling up server disks.
- **Internal Working**: Logback processes log levels (INFO, WARN, ERROR), writing logs to files and deleting files older than rotation limits.
- **ASCII Flow Diagram**:
  ```
  Log Event ──► [Logback Router] ──► Prod File Output (Size-based rotation) ──► Archive / Compression
  ```
- **Related Project Classes**: [application.yml](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/resources/application.yml)
- **Related Technologies**: SLF4J, Logback, JSON Log Layouts
- **Common Follow-up Questions**:
  1. What is the difference between synchronous and asynchronous logging?
  2. How do you configure log levels per environment?
  3. Why are JSON log formats preferred in production?
- **Common Mistakes**: Leaving logging levels set to DEBUG in production, which blocks threads and fills up disk space rapidly.
- **Production Best Practice**: Compress archived logs and forward them to centralized logging systems (like ELK).
- **Real Example from project**: [application.yml](file:///c:/Users/kshit/eclipse-workspace/Project/backend/src/main/resources/application.yml) defines package-specific logging levels.

---

### Q52: How do you monitor JVM health, memory allocation, and GC pauses?
- **Why interviewer asks this**: To evaluate your JVM diagnostics, monitoring tooling, and runtime performance tuning experience.
- **Expected Interview Answer**: I monitor JVM health using **Spring Boot Actuator** and forward metrics to Prometheus and Grafana. I track heap usage, active threads, GC pause times, and configure alerts for OutOfMemory events.
- **Simple Explanation**: Using monitoring endpoints and visual dashboards to track memory usage and garbage collection pauses in production.
- **Internal Working**: Actuator collects JVM statistics from MXBeans, exposing them in Prometheus formats to build metrics dashboards.
- **ASCII Flow Diagram**:
  ```
  JVM Statistics ──► [Spring Boot Actuator] ──► Prometheus Scraper ──► Grafana Dashboard
  ```
- **Related Project Classes**: [pom.xml](file:///c:/Users/kshit/eclipse-workspace/Project/backend/pom.xml)
- **Related Technologies**: Spring Boot Actuator, Prometheus, Micrometer
- **Common Follow-up Questions**:
  1. What metrics does the /actuator/prometheus endpoint expose?
  2. How do you identify heap memory leaks in dashboards?
  3. What is the performance impact of JVM Actuator scraping?
- **Common Mistakes**: Leaving monitoring endpoints open to public access, exposing internal server details to security risks.
- **Production Best Practice**: Secure monitoring endpoints behind admin authorization checks.
- **Real Example from project**: [pom.xml](file:///c:/Users/kshit/eclipse-workspace/Project/backend/pom.xml) includes Actuator dependencies to expose JVM statistics.
