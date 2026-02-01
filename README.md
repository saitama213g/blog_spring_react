# Blog Application (Spring Boot + React)

A full-stack **Blog Application** built with **Spring Boot** for the backend and **React (TypeScript)** for the frontend. The project follows a clean REST architecture, supports authentication, post and category management, and is designed to be scalable and easy to extend.

---

## 🧱 Tech Stack

### Backend

* Java 17+
* Spring Boot
* Spring Web (REST API)
* Spring Data JPA
* Spring Security (JWT-based authentication)
* Hibernate
* DTO mapping with Mappers (Entity ↔ DTO separation)
* PostgreSQL / MySQL (configurable)
* Maven

### Frontend

* React
* TypeScript
* Axios
* React Router
* Vite / Create React App

---

## ✨ Features

* Clean architecture using **DTOs and Mappers** to separate API models from persistence entities

* User authentication (Register / Login) with JWT

* Create, update, delete blog posts

* Categories and tags support

* RESTful API design

* Entity ↔ DTO mapping to avoid exposing database models directly

## ⚙️ Backend Setup (Spring Boot)

### 1. Clone the repository

```bash
git clone https://github.com/your-username/blog-project.git
cd blog-project/backend
```

### 2. Configure the database

Update `application.yml` or `application.properties`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blog_db
    username: postgres
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: your_secret_key
  expiration: 86400000
```

### 3. Run the backend

```bash
mvn spring-boot:run
```

Backend will start at:

```
http://localhost:8080
```

---

## 🎨 Frontend Setup (React)

### 1. Navigate to frontend

```bash
cd ../blogfrontend
```

### 2. Install dependencies

```bash
npm install
```

### 3. Configure API base URL

Example (`src/services/api.ts`):

```ts
export const API_BASE_URL = "http://localhost:8080/api/v1";
```

### 4. Run the frontend

```bash
npm run dev
```

Frontend will be available at:

```
http://localhost:5173
```

---

## 🔐 Authentication Flow

1. User logs in or registers
2. Backend returns a JWT token
3. Token is stored in `localStorage`
4. Axios interceptor attaches token to each request
5. Spring Security validates the token
