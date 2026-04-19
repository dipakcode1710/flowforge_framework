# 🚀 FlowForge Framework – Progress & Feature Documentation

## 📌 Overview

FlowForge is a lightweight Java backend framework designed for rapid API development with minimal configuration.
It provides a clean developer experience similar to modern frameworks like Spring Boot, but with a simplified internal architecture.

---

# ✅ Core Features Implemented

## 1. 🌐 Embedded HTTP Server

* Built using Java `HttpServer`
* Automatic startup with configurable port
* Handles routing and request lifecycle

---

## 2. 🔀 Routing System

Supports annotation-based routing:

```java
@Get("/user")
@Post("/create")
```

### Features:

* HTTP method mapping (GET, POST)
* Dynamic path variables (`/user/{id}`)
* Query parameter support

---

## 3. 🧠 Dependency Injection (DI Container)

### Supported:

```java
@Inject
private UserService service;
```

### Capabilities:

* Automatic bean creation
* Recursive dependency injection
* Context-based bean storage
* Lazy + eager resolution

---

## 4. 🧩 Bean System

### Supported annotations:

```java
@Component
@Service
@Controller
@ConfigurationProperties
```

### Behavior:

* Automatic class scanning
* Bean registration in context
* Generic DI support (not limited to controllers/services)

---

## 5. ⚙️ Configuration System

### Features:

* `app.properties` support
* Profile-based configs (`app-dev.properties`, `app-prod.properties`)
* Runtime config resolution

---

## 6. 💉 @Value Injection

```java
@Value(value = "app.name", defaultValue = "DefaultApp")
private String appName;
```

### Supports:

* Type conversion
* Default values

---

## 7. 📦 @ConfigurationProperties

```java
@ConfigurationProperties("server")
public class ServerConfig {
    public int port;
    public String name;
}
```

### Features:

* Prefix-based binding
* Automatic field mapping
* Strongly typed config

---

## 8. 🛡️ Middleware System

### Example:

```java
@Auth(role = "ADMIN")
```

### Capabilities:

* Request interception
* Chain-based execution
* Role-based authorization

---

## 9. 🔐 JWT Authentication

* Token generation
* Role-based access control
* Middleware integration

---

## 10. ✅ Validation System

```java
@Get("/test")
public String test(@QueryParam("id") @NotNull @Min(5) Integer id)
```

### Features:

* Annotation-based validation
* Runtime enforcement
* Integrated with exception handling

---

## 11. ⚠️ Advanced Exception Handling

```java
@ExceptionHandler(RuntimeException.class)
@ResponseStatus(400)
public ErrorResponse handle(RuntimeException e)
```

### Features:

* Multiple exception handlers
* Type-based resolution
* JSON error responses
* HTTP status control

---

## 12. 🛠️ Dev Dashboard

### 🔹 `/dev/routes`
Returns all registered routes as JSON.

### 🔹 `/dev/docs`
Returns all registered routes as JSON (Swagger-compatible format).

### 🔹 `/dev/docs-ui`
Visual interactive API documentation UI.

#### Features:
* Color-coded method badges (GET, POST, PUT, DELETE)
* Collapsible route cards
* Filter routes by HTTP method
* Per-param input fields that auto-build the URL
* Global JWT bearer token field shared across all requests
* Live "Try it" button per route with color-coded response box
* Handles both `@QueryParam` and `@PathVariable` automatically

---

# 🧪 Example Usage

```java
@Controller
public class UserController {

    @Inject
    private UserService service;

    @Get("/hello")
    public String hello() {
        return service.getMessage();
    }
}
```

---

# 🧠 Internal Architecture

```text
Flow.run()
   ↓
ClassScanner → finds classes
   ↓
Context → registers beans
   ↓
Injector → injects dependencies
   ↓
Dispatcher → maps routes
   ↓
Server → handles requests
```

---

# 📊 Progress vs Real Frameworks

| Feature            | FlowForge | Spring Boot |
| ------------------ | --------- | ----------- |
| DI Container       | ✅         | ✅           |
| Routing            | ✅         | ✅           |
| Config System      | ✅         | ✅           |
| Profiles           | ✅         | ✅           |
| Middleware         | ✅         | ✅           |
| Validation         | ✅         | ✅           |
| Exception Handling | ✅         | ✅           |
| Dev Dashboard      | ✅         | ✅           |
| Swagger-like UI    | ✅         | ✅           |
| ORM / Database     | ❌         | ✅           |
| AOP                | ❌         | ✅           |

---

# 📈 Completion Estimate

```text
Core Framework Completion: ~80%
```

👉 You have completed:

* All **core backend fundamentals**
* Most **developer experience features**
* Full **interactive API documentation UI**

---

# 🚀 What to Build Next (Priority Order)

## 🥇 1. /dev/beans Dashboard

* Show all registered beans
* Debug DI issues easily

## 🥈 2. @Component Enhancements

* Bean scopes (singleton/prototype)
* Lazy initialization

## 🥉 3. Database Layer (ORM-lite)

* Basic repository support
* `@Repository` annotation
* Simple CRUD operations

## 🏗️ 4. AOP Support

* `@Before` / `@After` method interceptors
* Logging and timing aspects

---

# 🎯 Summary

FlowForge now supports:

```text
✔ Dependency Injection
✔ Routing
✔ Config Management
✔ Middleware & Auth
✔ Validation
✔ Exception Handling
✔ Dev Dashboard
✔ Component System
✔ Interactive API Docs UI (/dev/docs-ui)
```

👉 This is no longer a prototype.
👉 This is a **functional backend framework core**.
👉 The `/dev/docs-ui` brings it on par with production frameworks for developer experience.

---

# 👨‍💻 Author

Dipak
