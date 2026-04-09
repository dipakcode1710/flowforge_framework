# 🚀 FlowForge - Lightweight Java Framework

FlowForge is a lightweight Java backend framework inspired by Spring Boot, designed to make development faster with minimal configuration and simple APIs.

---

## ✨ Features

* ⚡ Embedded HTTP Server (no external server needed)
* 🔀 Annotation-based routing (`@Get`, `@Post`)
* 📦 JSON request & response support
* 🧠 Reflection-based method invocation
* 🧩 Simple and extensible architecture
* 🚫 No complex configuration required

---

## 📁 Project Structure

```
flowforge/
 ├── core/
 │    ├── annotations/
 │    ├── dispatcher/
 │    ├── server/
 │    └── context/
 ├── Flow.java
example/
 ├── AppMain.java
 ├── UserController.java
 └── User.java
```

---

## 🚀 Getting Started

### 1. Clone the repository

```
git clone <your-repo-url>
cd flowforge
```

---

### 2. Run the application

Run the main class:

```
example.AppMain
```

---

### 3. Open in browser

```
http://localhost:8080/hello
http://localhost:8080/user
```

---

## 🧩 Example

### Controller

```java
@Controller
public class UserController {

    @Get("/hello")
    public String hello() {
        return "Hello from FlowForge!";
    }

    @Post("/save")
    public String save(String body) {
        return "Received: " + body;
    }

    @Get("/user")
    public User getUser() {
        return new User("Dipak", 25);
    }
}
```

---

### Model

```java
public class User {
    public String name;
    public int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

---

## 🧪 Testing APIs

### GET request

```
http://localhost:8080/hello
```

---

### POST request (using curl)

```
curl -X POST http://localhost:8080/save \
-H "Content-Type: application/json" \
-d '{"name":"Dipak"}'
```

---

## ⚙️ How It Works

1. Server starts using built-in `HttpServer`
2. Dispatcher scans controller annotations
3. Routes are mapped (`GET:/path`, `POST:/path`)
4. Incoming requests are matched and executed via reflection
5. Responses are returned as String or JSON

---

## 🔥 Current Capabilities

* ✅ GET & POST routing
* ✅ JSON request body handling
* ✅ Object → JSON response conversion
* ✅ Basic framework structure

---

## 🚧 Upcoming Features

* 🔄 Auto controller scanning (no manual registration)
* 🔌 Dependency Injection (`@Inject`)
* ⚙️ Configuration system (properties / YAML)
* 📦 Proper JSON handling using Jackson
* 🧵 Middleware / Filters
* 🧪 Validation support

---

## 🤝 Contributing

Contributions are welcome! Feel free to fork and improve the framework.

---

## 📜 License

This project is open-source and available under the MIT License.

---

## 💡 Inspiration

Inspired by modern frameworks like Spring Boot, but focused on simplicity and developer experience.

---

## 👨‍💻 Author

Dipak

---

⭐ If you like this project, give it a star!
