# 🚀 FlowForge Framework – Usage Guide

FlowForge is a lightweight Java backend framework designed for fast API development with minimal setup and clean developer experience.

This guide explains how to **use the framework**, not how it is built.

---

# 📦 What FlowForge Provides

FlowForge handles everything automatically:

* HTTP server startup
* Routing of requests
* JSON request/response handling
* Dependency injection
* Service management
* Dynamic URL handling

---

# 🚀 How to Start a FlowForge Application

1. Create a main application entry point
2. Run the application
3. Framework starts the server automatically

Once started, the application is available on:

```text
http://localhost:8080
```

---

# 🔀 Defining APIs

FlowForge uses annotations to define endpoints.

## GET APIs

* Used for fetching data
* Accessible via browser or API tools

Example usage:

* Access endpoints directly via URL
* Returns plain text or JSON

---

## POST APIs

* Used for sending data to server
* Accepts JSON input
* Returns JSON response

To call:

* Use tools like Postman or curl
* Set header: `Content-Type: application/json`
* Send JSON body

---

# 📦 JSON Handling

FlowForge automatically:

* Converts request JSON → Java objects
* Converts Java objects → JSON response

### Behavior

| Input        | Output               |
| ------------ | -------------------- |
| JSON request | Java object          |
| Java object  | JSON response        |
| Invalid JSON | Error response (400) |

---

# 🔗 Dynamic URLs (Path Variables)

FlowForge supports dynamic routing using URL values.

### Example usage

```text
/user/10
/user/10/order/500
```

### Behavior

* Values in URL are automatically extracted
* Passed to API internally
* Supports multiple values

---

# 🔢 Type Conversion

FlowForge automatically converts URL values into correct types:

| URL Value | Type   |
| --------- | ------ |
| `10`      | int    |
| `1000`    | long   |
| `10.5`    | double |
| text      | String |

---

# 🔌 Dependency Injection

FlowForge automatically manages dependencies.

### Behavior

* Services are created automatically
* Injected where required
* No manual object creation needed

---

# 🧩 Service Layer

Used to separate business logic.

### Benefits

* Cleaner code
* Reusable logic
* Better structure

FlowForge automatically detects and manages services.

---

# 🔍 Auto Detection

FlowForge automatically detects:

* Controllers
* Services

No manual registration is required.

---

# 🌐 API Response Behavior

| Scenario        | Response  |
| --------------- | --------- |
| Success         | 200 OK    |
| Invalid JSON    | 400 Error |
| Route not found | 404 Error |
| Internal error  | 500 Error |

---

# 🧪 Testing APIs

You can test APIs using:

* Browser (for GET)
* Postman
* curl

---

# 📁 Project Setup Requirements

To use FlowForge:

* Add FlowForge library to project
* Add Jackson dependencies
* Create application and APIs

No additional configuration is required.

---

# ⚡ Key Advantages

* No configuration files required
* Minimal setup
* Fast API development
* Clean architecture
* Lightweight compared to traditional frameworks

---

# 🚧 Current Capabilities

* Routing (GET/POST)
* JSON handling
* Dependency Injection
* Service layer support
* Path variables (multi + typed)
* Embedded server

---

# 🔮 Upcoming Features

* Query parameters support
* Configuration system
* Middleware support
* Validation system
* Advanced error handling

---

# 🎯 Summary

FlowForge allows developers to:

* Build APIs quickly
* Avoid boilerplate code
* Focus on business logic
* Run applications instantly

---

# 👨‍💻 Author

Dipak

---

# ⭐ Support

If you find FlowForge useful, consider supporting the project.
