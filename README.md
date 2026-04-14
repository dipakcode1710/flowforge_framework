# 🚀 FlowForge Framework – Usage Guide

FlowForge is a lightweight Java backend framework designed for fast API development with minimal setup and a clean developer experience.

This guide explains how to **use the framework**, not how it is built.

---

# 📦 What FlowForge Provides

FlowForge handles everything automatically:

- HTTP server startup
- Request routing (GET/POST)
- JSON request/response handling
- Dependency injection
- Service lifecycle management
- Dynamic URL handling (Path Variables)
- Query parameter handling
- Middleware system
- Authentication & Authorization (JWT-based)

---

# 🚀 How to Start a FlowForge Application

1. Create a main application entry point
2. Run the application
3. Framework starts the server automatically

Application runs on:

http://localhost:8080

---

# 🔀 Defining APIs

FlowForge uses annotations to define endpoints.

---

## 📥 GET APIs

- Used for fetching data
- Can be accessed via browser or API tools

---

## 📤 POST APIs

- Used for sending data to server
- Accepts JSON input
- Returns JSON response

### To call POST APIs:

- Use Postman or curl
- Add header:

Content-Type: application/json

- Send JSON body

---

# 📦 JSON Handling

FlowForge automatically:

- Converts request JSON → Java objects
- Converts Java objects → JSON response

### Behavior

| Input        | Output               |
|--------------|--------------------|
| JSON request | Java object        |
| Java object  | JSON response      |
| Invalid JSON | 400 Error response |

---

# 🔗 Dynamic URLs (Path Variables)

Supports dynamic routing using URL values.

### Example URLs

/user/10  
/user/10/order/500  

### Behavior

- Values are automatically extracted
- Supports multiple variables
- Automatically type converted

---

# 🔍 Query Parameters

FlowForge supports query-based inputs.

### Example

/user?id=10  
/search?q=phone&page=2  

### Behavior

- Automatically extracted from URL
- Supports required and optional parameters

---

# 🔢 Type Conversion

FlowForge automatically converts values into correct types:

| Value  | Type   |
|--------|--------|
| 10     | int    |
| 1000   | long   |
| 10.5   | double |
| text   | String |

---

# 🔌 Dependency Injection

FlowForge automatically manages dependencies.

### Behavior

- Services are auto-created
- Injected wherever needed
- No manual object creation required

---

# 🧩 Service Layer

Used to separate business logic.

### Benefits

- Clean architecture
- Reusable logic
- Better maintainability

---

# 🔍 Auto Detection

FlowForge automatically detects:

- Controllers
- Services

No manual registration required.

---

# ⚙️ Middleware System

FlowForge provides a powerful middleware system.

### Capabilities

- Execute logic before request
- Modify request/response
- Stop request execution
- Chain multiple middlewares

### Common Use Cases

- Logging
- Authentication
- Rate limiting
- Request validation

---

# 🔐 Authentication (JWT Based)

FlowForge supports JWT-based authentication.

### How it works

- APIs can be protected
- Requests must include a valid JWT token

### Example header

Authorization: Bearer <token>

---

# 🛡️ Role-Based Authorization

FlowForge supports role-based access control.

### Behavior

- APIs can require specific roles
- Role is extracted from JWT
- Access is granted or denied automatically

### Example roles

- USER
- ADMIN

---

# 🌐 API Response Behavior

| Scenario              | Response  |
|----------------------|----------|
| Success              | 200 OK   |
| Invalid JSON         | 400 Error|
| Unauthorized         | 401 Error|
| Forbidden (role)     | 403 Error|
| Route not found      | 404 Error|
| Internal error       | 500 Error|

---

# 🧪 Testing APIs

You can test APIs using:

- Browser (GET requests)
- Postman
- curl

---

# 📁 Project Setup Requirements

To use FlowForge:

- Add FlowForge library
- Add Jackson dependencies
- Add JWT dependencies (for auth)

No configuration files required.

---

# ⚡ Key Advantages

- Zero configuration
- Lightweight
- Fast development
- Clean architecture
- Annotation-driven design
- Built-in auth & middleware

---

# 🚧 Current Capabilities

- Routing (GET/POST)
- JSON handling
- Dependency Injection
- Service layer support
- Path variables (multi + typed)
- Query parameters
- Middleware system
- JWT Authentication 🔥
- Role-based Authorization 🔥

---

# 🔮 Upcoming Features

- Configuration system (app.properties)
- Validation framework
- Global middleware
- API documentation UI
- Exception handling improvements

---

# 🎯 Summary

FlowForge allows developers to:

- Build APIs quickly
- Avoid boilerplate code
- Focus on business logic
- Build secure APIs with JWT
- Scale with clean architecture

---

# 👨‍💻 Author

Dipak

---

# ⭐ Support

If you find FlowForge useful, consider supporting the project.
