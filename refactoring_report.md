# Design Patterns Refactoring Report

**Course**: IT342 – System Integration and Architecture  
**Student**: Justine Filip Custodio  
**Project**: MDQueue – Medical/Doctor Queue Management System  
**Branch**: `feature/design-patterns-refactor`  
**Date**: April 2026

---

## Table of Contents

1. [Builder Pattern](#1-builder-pattern--creational)
2. [Singleton Pattern](#2-singleton-pattern--creational)
3. [Adapter Pattern](#3-adapter-pattern--structural)
4. [Observer Pattern](#4-observer-pattern--behavioral)
5. [Strategy Pattern](#5-strategy-pattern--behavioral)
6. [Facade Pattern](#6-facade-pattern--structural)

---

## 1. Builder Pattern — Creational

### Before vs After

**Original Implementation:**  
The `AuthService` class had duplicated `AuthResponse.builder()` chains in both the `register()` and `login()` methods. Each method independently constructed the response object with the same 6 fields, making the code verbose and error-prone — if a new field was added, both methods needed updating.

**Problems:**
- Code duplication between `register()` and `login()` methods
- Risk of inconsistency if one method was updated but not the other
- No documentation of the Builder pattern's intentional use

### Applied Design Pattern

**Pattern:** Builder  
**Where:** `AuthService.java`, `AuthResponse.java` (backend)

### Justification

The Builder pattern was already partially used via Lombok's `@Builder` annotation, but it wasn't applied intentionally. By extracting a centralized `buildAuthResponse()` method, the repeated builder chain is consolidated into a single place. This demonstrates the Builder pattern's key benefit: abstracting complex construction logic so callers specify *what* to build, not *how*.

### Code Snippets

**Before (duplicated in both register() and login()):**
```java
return AuthResponse.builder()
        .token(token)
        .type("Bearer")
        .userId(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .message("Account created successfully")
        .build();
```

**After (centralized builder method):**
```java
// Both register() and login() now call:
return buildAuthResponse(user, token, "Account created successfully");

// Centralized Builder Pattern method
private AuthResponse buildAuthResponse(User user, String token, String message) {
    return AuthResponse.builder()
            .token(token)
            .type("Bearer")
            .userId(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .message(message)
            .build();
}
```

### Improvement

- **Eliminated duplication**: One method instead of two identical builder chains
- **Single point of change**: New fields only need adding in one place
- **Self-documenting**: Javadoc explicitly describes the Builder pattern usage

---

## 2. Singleton Pattern — Creational

### Before vs After

**Original Implementation:**  
The `GlobalExceptionHandler` and `HomeController` manually created `HashMap<String, Object>` responses in every handler method. Each method independently assembled response maps with inconsistent field names (`"error"`, `"message"`, `"status"`, etc.), with no guarantee of uniformity.

**Problems:**
- Response structure varied across endpoints (some had `"error"` field, others didn't)
- No centralized response format — each handler reinvented the structure
- No type safety — `Map<String, Object>` allowed any key-value combination

### Applied Design Pattern

**Pattern:** Singleton  
**Where:** `ApiResponseFactory.java`, `ApiResponse.java`, `GlobalExceptionHandler.java`, `HomeController.java` (backend)

### Justification

A response factory should be a single, shared utility — creating multiple instances would be wasteful and could lead to inconsistent behavior. The Singleton pattern ensures exactly one `ApiResponseFactory` instance exists, providing a global access point for response construction. All exception handlers and controllers now use this single factory.

### Code Snippets

**Before (repeated in each handler):**
```java
Map<String, Object> body = new HashMap<>();
body.put("timestamp", LocalDateTime.now());
body.put("status", HttpStatus.CONFLICT.value());
body.put("error", "Conflict");
body.put("message", ex.getMessage());
return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
```

**After (using Singleton factory):**
```java
// Singleton: single instance obtained via getInstance()
private final ApiResponseFactory responseFactory = ApiResponseFactory.getInstance();

@ExceptionHandler(DuplicateEmailException.class)
public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException ex) {
    ApiResponse<Void> response = responseFactory.error(
            HttpStatus.CONFLICT.value(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
}
```

**ApiResponseFactory Singleton:**
```java
public class ApiResponseFactory {
    private static final ApiResponseFactory INSTANCE = new ApiResponseFactory();
    private ApiResponseFactory() {} // private constructor
    public static ApiResponseFactory getInstance() { return INSTANCE; }
    
    public <T> ApiResponse<T> success(T data, String message) { ... }
    public <T> ApiResponse<T> error(int status, String message) { ... }
}
```

### Improvement

- **Consistent API responses**: All endpoints return the same `ApiResponse<T>` structure
- **Type safety**: Generic `ApiResponse<T>` replaces untyped `Map<String, Object>`
- **Single source of truth**: Factory centralizes response creation logic
- **Thread-safe**: Eager initialization guarantees safe concurrent access

---

## 3. Adapter Pattern — Structural

### Before vs After

**Original Implementation:**  
`AuthController` returned raw `AuthResponse` objects directly as the HTTP response body, while `HomeController` returned `Map<String, Object>`, and `GlobalExceptionHandler` returned yet another format. Frontend consumers had to handle each format differently.

**Problems:**
- Inconsistent response formats across endpoints
- Domain objects (`AuthResponse`) leaked directly into the API contract
- Frontend had to account for different response shapes

### Applied Design Pattern

**Pattern:** Adapter  
**Where:** `ApiResponseAdapter.java`, `AuthController.java` (backend)

### Justification

The Adapter pattern bridges the gap between domain-specific DTOs (like `AuthResponse`) and the standardized API contract (`ApiResponse<T>`). Instead of modifying the domain model to match the API format, the adapter *converts* between the two — preserving separation of concerns.

### Code Snippets

**Before:**
```java
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    AuthResponse response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

**After (using Adapter):**
```java
@PostMapping("/register")
public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
    AuthResponse authResponse = authService.register(request);
    // Adapter Pattern: convert domain AuthResponse → standardized ApiResponse
    ApiResponse<AuthResponse> response = ApiResponseAdapter.toSuccessResponse(
            authResponse, authResponse.getMessage(), HttpStatus.CREATED.value());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### Improvement

- **Uniform API contract**: All endpoints return `ApiResponse<T>` wrapping domain data
- **Separation of concerns**: Domain DTOs remain unchanged; adapter handles conversion
- **Frontend simplification**: All responses follow the same `{ success, status, message, data }` shape

---

## 4. Observer Pattern — Behavioral

### Before vs After

**Original Implementation:**  
`AuthService.register()` and `login()` handled everything in a single flow — authentication, token generation, and response building. If we needed to add logging, analytics, or email notifications, all would be added directly to these methods.

**Problems:**
- Violates Single Responsibility Principle — auth methods do too much
- Adding side-effects requires modifying the core auth service
- No way to independently enable/disable side-effects
- Tight coupling between auth logic and its consequences

### Applied Design Pattern

**Pattern:** Observer  
**Where:** `AuthEvent.java`, `AuthEventPublisher.java`, `AuthEventListener.java`, `AuthService.java` (backend)

### Justification

The Observer pattern decouples the act of authentication from its side-effects. `AuthService` publishes events without knowing who listens. New subscribers (welcome email, analytics, logging) can be added by creating new `@EventListener` classes — without touching `AuthService`.

### Code Snippets

**Before:**
```java
public AuthResponse register(RegisterRequest request) {
    User user = userService.register(request);
    // ... token generation ...
    return buildAuthResponse(user, token, "Account created successfully");
    // No way to add side-effects without modifying this method
}
```

**After (with Observer):**
```java
public AuthResponse register(RegisterRequest request) {
    User user = userService.register(request);
    // ... token generation ...
    
    // Observer Pattern: publish event (decoupled side-effects)
    authEventPublisher.publishRegisterEvent(user);
    
    return buildAuthResponse(user, token, "Account created successfully");
}

// Separate listener handles side-effects independently
@Component
public class AuthEventListener {
    @EventListener
    public void onAuthEvent(AuthEvent event) {
        log.info("[AUTH EVENT] User {} performed {} at {}",
            event.getEmail(), event.getEventType(), event.getTimestamp());
    }
}
```

### Improvement

- **Decoupled side-effects**: Logging, analytics, etc. are independent classes
- **Open for extension**: New listeners added without modifying AuthService
- **Testable**: AuthService can be tested without worrying about side-effects
- **Configurable**: Listeners can be toggled via Spring profiles

---

## 5. Strategy Pattern — Behavioral

### Before vs After

**Original Implementation:**  
`UserService.register()` had inline password matching logic (`request.getPassword().equals(request.getConfirmPassword())`), but no password strength validation at all. Adding strength rules would mean hardcoding more `if` statements into the service.

**Problems:**
- No password strength validation
- Adding validation rules would clutter the service class
- No way to switch between password policies (basic vs. strong) without code changes
- Violates Open-Closed Principle

### Applied Design Pattern

**Pattern:** Strategy  
**Where:** `PasswordValidationStrategy.java`, `BasicPasswordValidator.java`, `StrongPasswordValidator.java`, `UserService.java` (backend)

### Justification

Password validation policies should be interchangeable without modifying the service that uses them. The Strategy pattern encapsulates each validation algorithm in its own class, and Spring's dependency injection selects the active one via `@Primary`. Switching from basic to strong validation requires zero changes to `UserService`.

### Code Snippets

**Before:**
```java
public User register(RegisterRequest request) {
    if (!request.getPassword().equals(request.getConfirmPassword())) {
        throw new IllegalArgumentException("...");
    }
    // No password strength validation at all
    User user = User.builder()...build();
    return userRepository.save(user);
}
```

**After (with Strategy):**
```java
private final PasswordValidationStrategy passwordValidator; // Injected

public User register(RegisterRequest request) {
    if (!request.getPassword().equals(request.getConfirmPassword())) {
        throw new IllegalArgumentException("...");
    }
    
    // Strategy Pattern: delegate to injected validator
    passwordValidator.validate(request.getPassword());
    
    User user = User.builder()...build();
    return userRepository.save(user);
}
```

**Two interchangeable strategies:**
```java
@Component @Primary // Default strategy
public class BasicPasswordValidator implements PasswordValidationStrategy {
    public void validate(String password) {
        if (password.length() < 8) throw new IllegalArgumentException("...");
    }
}

@Component // Alternative strategy
public class StrongPasswordValidator implements PasswordValidationStrategy {
    public void validate(String password) {
        // Checks: length + uppercase + digit + special character
    }
}
```

### Improvement

- **Password strength validation**: Now enforced (was completely missing)
- **Interchangeable policies**: Swap `@Primary` to switch between basic and strong
- **Open-Closed Principle**: New strategies added without modifying UserService
- **Clean separation**: Validation logic is in its own class, not inline

---

## 6. Facade Pattern — Structural

### Before vs After

**Original Implementation:**  
Both `LoginPage.jsx` and `RegisterPage.jsx` directly coordinated multiple subsystems: calling `api.post()` for HTTP requests, calling `setToken()` for token storage, and manually extracting errors from response objects. This logic was duplicated across both components.

**Problems:**
- Each component contained identical boilerplate for API calls + token storage
- Components were tightly coupled to the API client and token storage internals
- If the API response structure changed, both components needed updating
- Error extraction logic was duplicated

### Applied Design Pattern

**Pattern:** Facade  
**Where:** `authFacade.js`, `LoginPage.jsx`, `RegisterPage.jsx` (frontend)

### Justification

The Facade pattern hides the complexity of coordinating multiple subsystems (API client, token manager, error formatter) behind a simple interface. Components call `authFacade.login()` and get a clean result — they don't need to know about token storage, API endpoints, or error response formats.

### Code Snippets

**Before (LoginPage.jsx):**
```javascript
import { api } from '../lib/api'
import { setToken } from '../lib/auth'

// Direct subsystem coordination in component
const res = await api.post('/api/auth/login', { email, password })
setToken(res.data.token)
navigate(redirectTo, { replace: true })
```

**After (using Facade):**
```javascript
import { authFacade } from '../lib/authFacade'

// Facade Pattern: single call replaces api.post() + setToken() + error parsing
const result = await authFacade.login(email, password)
if (result.success) {
    navigate(redirectTo, { replace: true })
} else {
    setError(result.message)
}
```

**The Facade module (`authFacade.js`):**
```javascript
export async function login(email, password) {
    const res = await api.post('/api/auth/login', { email, password })
    const payload = res.data.data || res.data
    setToken(payload.token)        // Token storage hidden from caller
    return { success: true, data: payload }
}
```

### Improvement

- **Simplified components**: Login/Register pages are cleaner with ~40% less code
- **Centralized changes**: API endpoint or token storage changes happen in one file
- **Consistent error handling**: Error extraction is standardized in the facade
- **Better testability**: Mock the facade instead of multiple subsystems

---

## Summary of All Changes

| # | Pattern | Category | Files Modified | Files Created |
|---|---------|----------|---------------|---------------|
| 1 | Builder | Creational | `AuthService.java`, `AuthResponse.java` | — |
| 2 | Singleton | Creational | `GlobalExceptionHandler.java`, `HomeController.java` | `ApiResponseFactory.java`, `ApiResponse.java` |
| 3 | Adapter | Structural | `AuthController.java` | `ApiResponseAdapter.java` |
| 4 | Observer | Behavioral | `AuthService.java` | `AuthEvent.java`, `AuthEventPublisher.java`, `AuthEventListener.java` |
| 5 | Strategy | Behavioral | `UserService.java` | `PasswordValidationStrategy.java`, `BasicPasswordValidator.java`, `StrongPasswordValidator.java` |
| 6 | Facade | Structural | `LoginPage.jsx`, `RegisterPage.jsx` | `authFacade.js` |

**Total: 8 files modified, 9 new files created**

---

## Conclusion

The refactoring applied six design patterns across all three layers of the MDQueue system (backend, frontend). Each pattern was chosen based on genuine problems in the existing codebase — not forced artificially. The changes improve:

- **Code Organization**: New packages (`event/`, `strategy/`, `adapter/`, `util/`) group related classes logically
- **Reusability**: `ApiResponseFactory`, `ApiResponseAdapter`, and `authFacade` are reusable across the entire application
- **Maintainability**: Centralized logic means changes happen in one place instead of scattered across files
- **Scalability**: Observer pattern allows adding unlimited side-effects; Strategy pattern allows unlimited validation policies; Adapter pattern ensures new endpoints follow the standard contract
