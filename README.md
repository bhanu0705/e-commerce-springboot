# 🛒 E-Commerce Microservices Project

> A production-style Spring Boot microservices architecture with a React frontend - perfect for learning and interview preparation.

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Interview Talking Points](#interview-talking-points)
- [Key Concepts Explained](#key-concepts-explained)

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                      Frontend (React)                           │
│                    http://localhost:5173                        │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway (8080)                           │
│          Routes requests to appropriate microservices           │
└───────────────────────────┬─────────────────────────────────────┘
                            │
            ┌───────────────┼───────────────┐
            ▼               ▼               ▼
     ┌──────────┐    ┌──────────┐    ┌──────────┐
     │ Product  │    │  Order   │    │   User   │
     │ Service  │◄───│ Service  │───►│ Service  │
     │  (8081)  │    │  (8082)  │    │  (8083)  │
     └────┬─────┘    └────┬─────┘    └────┬─────┘
          │               │               │
          ▼               ▼               ▼
     ┌──────────────────────────────────────────┐
     │         Eureka Discovery Server          │
     │            http://localhost:8761         │
     └──────────────────────────────────────────┘
```

**Service Communication Flow:**

1. Client → API Gateway (single entry point)
2. API Gateway → Eureka (discovers service locations)
3. API Gateway → Target Service (forwards request)
4. Order Service → Product/User Services (via Feign clients)

---

## 🛠️ Tech Stack

### Backend

| Technology               | Purpose                       |
| ------------------------ | ----------------------------- |
| **Spring Boot 3.2**      | Application framework         |
| **Spring Cloud 2023.x**  | Microservices infrastructure  |
| **Netflix Eureka**       | Service discovery             |
| **Spring Cloud Gateway** | API Gateway                   |
| **OpenFeign**            | Declarative HTTP client       |
| **Spring Data JPA**      | Database access               |
| **H2 Database**          | In-memory database (dev)      |
| **Lombok**               | Reduce boilerplate code       |
| **Maven**                | Build & dependency management |

### Frontend

| Technology       | Purpose                 |
| ---------------- | ----------------------- |
| **React 18**     | UI library              |
| **Vite**         | Build tool & dev server |
| **React Router** | Client-side routing     |
| **Axios**        | HTTP client             |

---

## 📁 Project Structure

```
springboot/
├── backend/
│   ├── pom.xml                    # Parent POM (shared dependencies)
│   │
│   ├── discovery-server/          # Eureka Server
│   │   ├── src/main/java/com/ecommerce/discovery/
│   │   │   └── DiscoveryServerApplication.java
│   │   └── src/main/resources/application.yml
│   │
│   ├── api-gateway/              # Spring Cloud Gateway
│   │   ├── src/main/java/com/ecommerce/gateway/
│   │   │   └── ApiGatewayApplication.java
│   │   └── src/main/resources/application.yml
│   │
│   ├── product-service/          # Product Management
│   │   ├── src/main/java/com/ecommerce/product/
│   │   │   ├── model/           # Entity classes
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── service/         # Business logic
│   │   │   ├── controller/      # REST endpoints
│   │   │   └── exception/       # Error handling
│   │   └── src/main/resources/application.yml
│   │
│   ├── order-service/            # Order Management (with Feign)
│   │   ├── src/main/java/com/ecommerce/order/
│   │   │   ├── client/          # Feign clients (Product, User)
│   │   │   ├── model/           # Order, OrderItem entities
│   │   │   ├── dto/             # Request/Response DTOs
│   │   │   ├── repository/
│   │   │   ├── service/         # Orchestration logic
│   │   │   ├── controller/
│   │   │   └── exception/       # Feign exception handling
│   │   └── src/main/resources/application.yml
│   │
│   └── user-service/             # User Management
│       ├── src/main/java/com/ecommerce/user/
│       └── src/main/resources/application.yml
│
└── frontend/
    └── ecommerce-dashboard/      # React Application
        ├── src/
        │   ├── api/             # Axios API client
        │   ├── pages/           # Page components
        │   ├── App.jsx          # Main app with routing
        │   └── index.css        # Styling
        └── package.json
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Node.js 18+** (for frontend)

### Step 1: Start Backend Services

**Important:** Start services in this order!

```bash
# Terminal 1: Start Discovery Server FIRST (wait for it to fully start)
cd backend/discovery-server
mvn spring-boot:run

# Terminal 2: Start API Gateway
cd backend/api-gateway
mvn spring-boot:run

# Terminal 3: Start Product Service
cd backend/product-service
mvn spring-boot:run

# Terminal 4: Start Order Service
cd backend/order-service
mvn spring-boot:run

# Terminal 5: Start User Service
cd backend/user-service
mvn spring-boot:run
```

### Step 2: Verify Services

- **Eureka Dashboard:** http://localhost:8761
  - You should see all 4 services registered (API-GATEWAY, PRODUCT-SERVICE, ORDER-SERVICE, USER-SERVICE)

- **H2 Consoles:**
  - Product DB: http://localhost:8081/h2-console (JDBC URL: `jdbc:h2:mem:productdb`)
  - Order DB: http://localhost:8082/h2-console (JDBC URL: `jdbc:h2:mem:orderdb`)
  - User DB: http://localhost:8083/h2-console (JDBC URL: `jdbc:h2:mem:userdb`)

### Step 3: Start Frontend

```bash
cd frontend/ecommerce-dashboard
npm install
npm run dev
```

Open http://localhost:5173 in your browser.

---

## 📡 API Endpoints

All requests go through the **API Gateway** at `http://localhost:8080`

### Product Service

```
GET    /api/products           # Get all products
GET    /api/products/{id}      # Get product by ID
POST   /api/products           # Create product
PUT    /api/products/{id}      # Update product
DELETE /api/products/{id}      # Delete product
PATCH  /api/products/{id}/stock # Update stock quantity
```

### Order Service

```
GET    /api/orders             # Get all orders
GET    /api/orders/{id}        # Get order by ID
GET    /api/orders/user/{id}   # Get orders for user
POST   /api/orders             # Create order (calls Product & User services!)
PUT    /api/orders/{id}/status # Update order status
```

### User Service

```
GET    /api/users              # Get all users
GET    /api/users/{id}         # Get user by ID
POST   /api/users              # Create user
PUT    /api/users/{id}         # Update user
DELETE /api/users/{id}         # Delete user
```

### Example: Create an Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "shippingAddress": "123 Main St",
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 2, "quantity": 1}
    ]
  }'
```

---

## 💬 Interview Talking Points

### 1. "Why Microservices?"

> "I chose microservices to demonstrate scalability and independent deployment. Each service owns its data and can be scaled independently. For example, during a sale, we can scale the Order Service without affecting others."

### 2. "Explain Service Discovery"

> "I use Netflix Eureka as a service registry. Each service registers itself on startup. When the Order Service needs to call Product Service, it asks Eureka 'where is product-service?' and gets the actual IP:Port. This enables dynamic scaling - no hardcoded URLs."

### 3. "How do services communicate?"

> "For synchronous calls, I use OpenFeign - a declarative HTTP client. I just write an interface, and Feign generates the implementation. It integrates with Eureka for service discovery and supports load balancing automatically."

### 4. "What's the role of API Gateway?"

> "The Gateway is the single entry point for all clients. It handles:
>
> - **Routing**: Forwards `/api/products/**` to Product Service
> - **Load Balancing**: Distributes requests across service instances
> - **Cross-cutting concerns**: Could add authentication, rate limiting, logging"

### 5. "How do you handle failures?"

> "I've implemented:
>
> - **Global Exception Handlers**: Consistent error responses
> - **FeignException handling**: When Product Service is down, Order Service catches it and returns a meaningful error
> - **In production**: I'd add Circuit Breakers (Resilience4j) to fail fast and prevent cascading failures"

### 6. "Why DTO pattern?"

> "DTOs separate internal domain models from API contracts. If I change my database schema, I don't break clients. DTOs also let me:
>
> - Hide sensitive fields
> - Combine data from multiple entities
> - Apply validation rules for input"

### 7. "Transaction handling across services?"

> "In this demo, I use best-effort consistency. If stock update fails after order creation, we have inconsistency. In production, I'd consider:
>
> - **Saga Pattern**: Compensating transactions (if stock update fails, cancel order)
> - **Event-driven**: Use message queues for eventual consistency
> - **Distributed transactions**: 2PC (complex, avoided in microservices)"

---

## 🎓 Key Concepts Explained

### Layered Architecture

```
Controller → Service → Repository → Database
    ↓           ↓           ↓
Handles     Business    Data Access
HTTP        Logic       (CRUD)
```

### Annotations Cheat Sheet

| Annotation               | Purpose                            |
| ------------------------ | ---------------------------------- |
| `@RestController`        | Marks class as REST API controller |
| `@Service`               | Marks business logic class         |
| `@Repository`            | Marks data access class            |
| `@Entity`                | Maps class to database table       |
| `@FeignClient`           | Creates HTTP client for service    |
| `@EnableEurekaServer`    | Starts Eureka registry             |
| `@EnableDiscoveryClient` | Registers with Eureka              |

### Spring Data JPA Magic

```java
// Just declare the method, Spring creates the query!
List<Product> findByCategory(String category);
List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);
Optional<User> findByEmail(String email);
```

---

## 📝 License

This project is created for learning and interview preparation purposes.

---

**Happy Coding! 🚀**
