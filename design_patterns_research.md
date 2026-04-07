# Software Design Patterns Research Document

**Course**: IT342 – System Integration and Architecture  
**Student**: Justine Filip Custodio  
**Project**: MDQueue – Medical/Doctor Queue Management System  
**Date**: April 2026

---

## Introduction

Software design patterns are reusable solutions to commonly recurring problems in software design. They represent best practices refined over time by experienced developers and provide a shared vocabulary for communicating design decisions. Design patterns are not finished code—they are templates that describe how to solve a problem in various contexts.

Design patterns are typically classified into three categories:

- **Creational Patterns** – Deal with object creation mechanisms, providing flexibility in what gets created, who creates it, and how.
- **Structural Patterns** – Concerned with how classes and objects are composed to form larger structures while keeping them flexible and efficient.
- **Behavioral Patterns** – Focus on communication and responsibility between objects, defining how they interact and distribute work.

---

## Creational Patterns

### 1. Builder Pattern

**Category**: Creational

**Problem It Solves**:  
When an object requires many parameters to construct—some optional, some required—constructors become unwieldy. The "telescoping constructor" anti-pattern arises when you create multiple constructor overloads for different combinations of parameters. This makes code hard to read, easy to misuse (wrong parameter order), and difficult to maintain.

**How It Works**:  
The Builder pattern separates the construction of a complex object from its representation. It uses a dedicated builder class that provides a fluent API (method chaining) to set each property step by step, then calls a `build()` method to produce the final immutable object.

Key participants:
- **Product**: The complex object being built (e.g., `AuthResponse`)
- **Builder**: A nested static class or separate class that accumulates the configuration
- **Director** (optional): Orchestrates the building process using a builder

**Real-World Example**:  
In web/backend systems, building HTTP responses is a classic use case. Spring's `ResponseEntity.builder()`, OkHttp's `Request.Builder()`, and Retrofit's `Retrofit.Builder()` all use this pattern. For example, when constructing a JWT token response with multiple fields (token, type, userId, email, fullName, message), a builder prevents constructor confusion and allows readable, self-documenting object construction.

**Use Case in MDQueue**:  
MDQueue already uses the Builder pattern via Lombok's `@Builder` annotation on DTOs (`AuthResponse`, `LoginRequest`, `RegisterRequest`) and entities (`User`). During the refactoring, the Builder usage is enhanced in `AuthService.java` by extracting a dedicated `buildAuthResponse()` helper method that centralizes the repeated AuthResponse construction logic. This eliminates code duplication between the `register()` and `login()` methods, demonstrating intentional application of the Builder pattern.

```java
// Before: Duplicated builder calls in register() and login()
return AuthResponse.builder()
        .token(token).type("Bearer").userId(user.getId())
        .email(user.getEmail()).fullName(user.getFullName())
        .message("Account created successfully").build();

// After: Centralized builder method
private AuthResponse buildAuthResponse(User user, String token, String message) {
    return AuthResponse.builder()
            .token(token).type("Bearer").userId(user.getId())
            .email(user.getEmail()).fullName(user.getFullName())
            .message(message).build();
}
```

---

### 2. Singleton Pattern

**Category**: Creational

**Problem It Solves**:  
Some classes should have exactly one instance throughout the application's lifecycle—such as configuration managers, connection pools, logging services, or factory utilities. Without the Singleton pattern, multiple instances could lead to inconsistent state, wasted resources, or conflicting behavior (e.g., two loggers writing to the same file).

**How It Works**:  
The Singleton pattern ensures a class has only one instance and provides a global point of access to it. Common implementations include:

1. **Eager Initialization**: Instance created at class loading time
2. **Lazy Initialization with Double-Checked Locking**: Instance created on first use, thread-safe
3. **Enum-Based Singleton** (Java): Leverages the JVM's guarantee that enum values are instantiated once
4. **Kotlin `object` Declaration**: Language-level singleton support

Key characteristics:
- Private constructor prevents external instantiation
- A static method or field provides the single instance
- Thread safety must be considered in multithreaded environments

**Real-World Example**:  
In backend systems, database connection pools (e.g., HikariCP) use the Singleton pattern to maintain a single pool shared across all requests. In Android development, Retrofit API client instances are typically singletons to avoid creating multiple HTTP clients with redundant connection pools. Logging frameworks like SLF4J use the Singleton pattern for their LoggerFactory.

**Use Case in MDQueue**:  
MDQueue's mobile app already implements the Singleton pattern in `ApiClient.kt` using Kotlin's `object` keyword, ensuring a single Retrofit instance across the app. In the refactoring, a new `ApiResponseFactory` singleton is introduced in the Spring Boot backend. This factory centralizes the creation of standardized `ApiResponse` objects, replacing the scattered `new HashMap<>()` response construction throughout `GlobalExceptionHandler`. The Singleton ensures all API responses follow a consistent format through a single, shared factory instance.

```java
// Singleton factory for constructing API responses
public class ApiResponseFactory {
    private static final ApiResponseFactory INSTANCE = new ApiResponseFactory();
    
    private ApiResponseFactory() {} // private constructor
    
    public static ApiResponseFactory getInstance() { return INSTANCE; }
    
    public <T> ApiResponse<T> success(T data, String message) { ... }
    public <T> ApiResponse<T> error(int status, String message) { ... }
}
```

---

## Structural Patterns

### 3. Facade Pattern

**Category**: Structural

**Problem It Solves**:  
Complex subsystems with many interacting classes and APIs present a steep learning curve to clients. When a client (e.g., a React component) needs to coordinate calls across multiple modules (HTTP client, token storage, error parsing), the result is tightly coupled code that is hard to modify or test. If the underlying API changes, every component that directly calls it must be updated.

**How It Works**:  
The Facade pattern provides a simplified, unified interface to a complex subsystem. It doesn't add new functionality—instead, it delegates to existing subsystem classes while hiding complexity from the client. The facade acts as a "front desk" that routes requests to the appropriate internal services.

Key participants:
- **Facade**: The simplified interface that clients interact with
- **Subsystem Classes**: The underlying complex classes (API client, token manager, error formatter)
- **Client**: The code that uses the facade instead of interacting with subsystems directly

**Real-World Example**:  
In web development, SDKs and client libraries commonly use the Facade pattern. For example, Firebase's `firebase.auth().signInWithEmailAndPassword()` is a facade that internally handles HTTP requests, token storage, session management, and error normalization. Similarly, AWS SDK methods like `s3.upload()` hide the complexity of multipart uploads, retries, and authentication behind a single method call.

**Use Case in MDQueue**:  
The MDQueue React frontend previously had each page component (LoginPage, RegisterPage) directly calling `api.post()`, extracting tokens from responses, calling `setToken()`, and handling errors—all inline. The Facade pattern is applied by creating `authFacade.js`, which exposes clean `login(email, password)` and `register(data)` functions. These methods internally coordinate the API call, token storage, and error formatting, so components simply call `authFacade.login()` and receive a clean result.

```javascript
// Before (in LoginPage.jsx): Direct subsystem interaction
const res = await api.post('/api/auth/login', { email, password })
setToken(res.data.token)

// After: Using the Facade
import { authFacade } from '../lib/authFacade'
const result = await authFacade.login(email, password)
// result = { success: true, data: { token, userId, email, fullName } }
```

---

### 4. Adapter Pattern

**Category**: Structural

**Problem It Solves**:  
When different parts of a system produce data in incompatible formats, clients must contain format-specific conversion logic. In REST APIs, some endpoints might return raw `Map<String, Object>`, others return typed DTOs, and error responses follow yet another structure. This inconsistency makes it hard for frontend consumers to handle API responses uniformly.

**How It Works**:  
The Adapter pattern converts the interface of a class into another interface that clients expect. It acts as a bridge between two incompatible interfaces, wrapping an existing class with a new interface without modifying the original code.

Key participants:
- **Target**: The interface the client expects (e.g., standardized `ApiResponse<T>`)
- **Adaptee**: The existing class with an incompatible interface (e.g., `AuthResponse`, `Map<String, Object>`)
- **Adapter**: The class that wraps the adaptee and translates it to the target interface

**Real-World Example**:  
In enterprise systems, the Adapter pattern is ubiquitous. When integrating a legacy payment gateway that returns XML responses with a modern microservice expecting JSON, an adapter translates between formats. ORMs like Hibernate use adapters to convert between database result sets and Java objects. In Android, `RecyclerView.Adapter` adapts a data collection into views that the RecyclerView can display.

**Use Case in MDQueue**:  
MDQueue's backend had inconsistent response formats: `AuthController` returned `AuthResponse` objects, while `HomeController` returned `Map<String, Object>`, and `GlobalExceptionHandler` built its own `HashMap` responses. The Adapter pattern is applied by creating `ApiResponseAdapter`, which wraps domain-specific response objects (like `AuthResponse`) into a standardized `ApiResponse<T>` wrapper. This adapter serves as a bridge between the domain layer and the API contract.

```java
// Adapter converts domain-specific AuthResponse to standardized ApiResponse
public class ApiResponseAdapter {
    public static <T> ApiResponse<T> toSuccessResponse(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true).status(200)
                .message(message).data(data).build();
    }
}
```

---

## Behavioral Patterns

### 5. Observer Pattern

**Category**: Behavioral

**Problem It Solves**:  
When an action in one part of the system should trigger reactions in other parts, hardcoding these dependencies creates tight coupling. For example, if user registration should trigger a welcome email, an analytics event, and a log entry, adding all these calls directly to the registration method violates the Single Responsibility Principle and makes the code difficult to extend.

**How It Works**:  
The Observer pattern defines a one-to-many dependency between objects. When one object (the **Subject/Publisher**) changes state, all its dependents (**Observers/Subscribers**) are notified automatically. This allows adding new reactions without modifying the subject.

Key participants:
- **Subject/Publisher**: The object that holds state and sends notifications (e.g., `AuthEventPublisher`)
- **Observer/Subscriber**: Objects that react to state changes (e.g., `AuthEventListener`)
- **Event**: The notification payload carrying relevant data

**Real-World Example**:  
In Spring Boot, the `ApplicationEventPublisher` implements the Observer pattern for decoupled event handling. In frontend frameworks, React's `useEffect` hook and Redux's store subscriptions follow the observer pattern. Message brokers like RabbitMQ and Kafka are architectural implementations of this pattern. Real-time notification systems (push notifications, WebSocket updates) use observers to notify connected clients of server-side changes.

**Use Case in MDQueue**:  
MDQueue's `AuthService` previously handled the entire auth flow in a single method—authentication, token generation, and response building. Using Spring's built-in event system, the Observer pattern decouples side-effects from the core auth logic. After a successful login or registration, `AuthService` publishes an `AuthEvent`. Separate listener classes (`AuthEventListener`) subscribe to these events and handle logging, future analytics, or welcome notifications independently. New listeners can be added without touching `AuthService`.

```java
// Publisher (in AuthService): Fires event after successful auth
authEventPublisher.publishLoginEvent(user);

// Observer/Listener: Reacts to auth events independently
@Component
public class AuthEventListener {
    @EventListener
    public void onAuthEvent(AuthEvent event) {
        log.info("User {} performed {} at {}", 
            event.getEmail(), event.getEventType(), event.getTimestamp());
    }
}
```

---

### 6. Strategy Pattern

**Category**: Behavioral

**Problem It Solves**:  
When a class needs to support multiple algorithms or behaviors for the same operation, embedding all variations with conditional statements (if/else, switch) leads to bloated, hard-to-maintain code. Adding a new variation requires modifying existing code, violating the Open-Closed Principle.

**How It Works**:  
The Strategy pattern defines a family of algorithms, encapsulates each one in its own class, and makes them interchangeable. The context (client) holds a reference to a strategy interface and delegates the work to the concrete strategy implementation, which can be swapped at runtime.

Key participants:
- **Strategy Interface**: Defines the contract for the algorithm (e.g., `PasswordValidationStrategy`)
- **Concrete Strategies**: Individual implementations (e.g., `BasicPasswordValidator`, `StrongPasswordValidator`)
- **Context**: The class that uses the strategy (e.g., `UserService`)

**Real-World Example**:  
In e-commerce systems, payment processing uses the Strategy pattern—different payment methods (credit card, PayPal, cryptocurrency) implement a common `PaymentStrategy` interface. Sorting algorithms in collection frameworks, compression algorithms (ZIP, GZIP, LZ4), and authentication strategies (OAuth, JWT, API Key) all follow this pattern. Spring Security's `AuthenticationProvider` is a real-world Strategy implementation.

**Use Case in MDQueue**:  
MDQueue's `UserService` had password matching logic hardcoded inline (`request.getPassword().equals(request.getConfirmPassword())`), with no password strength validation. The Strategy pattern introduces a `PasswordValidationStrategy` interface with two implementations: `BasicPasswordValidator` (minimum 8 characters) and `StrongPasswordValidator` (requires uppercase, digit, and special character). The active strategy is injected into `UserService` via Spring's dependency injection, allowing the password policy to be changed by simply swapping the bean—without modifying any service code.

```java
// Strategy Interface
public interface PasswordValidationStrategy {
    void validate(String password) throws IllegalArgumentException;
}

// Concrete Strategy: Basic
@Component @Primary
public class BasicPasswordValidator implements PasswordValidationStrategy {
    public void validate(String password) {
        if (password.length() < 8) throw new IllegalArgumentException("...");
    }
}

// Context: UserService uses the injected strategy
@Service
public class UserService {
    private final PasswordValidationStrategy passwordValidator;
    
    public User register(RegisterRequest request) {
        passwordValidator.validate(request.getPassword()); // Strategy call
        // ... rest of registration
    }
}
```

---

## Summary Table

| # | Pattern Name | Category | Problem Solved | Applied In |
|---|-------------|----------|---------------|------------|
| 1 | Builder | Creational | Complex object construction with many parameters | Backend: `AuthService`, `AuthResponse` |
| 2 | Singleton | Creational | Ensuring single instance for global access | Backend: `ApiResponseFactory` |
| 3 | Facade | Structural | Simplifying complex subsystem interactions | Frontend: `authFacade.js` |
| 4 | Adapter | Structural | Converting incompatible interfaces | Backend: `ApiResponseAdapter` |
| 5 | Observer | Behavioral | Decoupling event producers from consumers | Backend: Auth event system |
| 6 | Strategy | Behavioral | Interchangeable algorithms without conditionals | Backend: Password validation |

---

## References

- Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley.
- Freeman, E., & Robson, E. (2020). *Head First Design Patterns* (2nd ed.). O'Reilly Media.
- Spring Framework Documentation – Application Events. https://docs.spring.io/spring-framework/reference/
- Refactoring Guru – Design Patterns. https://refactoring.guru/design-patterns
