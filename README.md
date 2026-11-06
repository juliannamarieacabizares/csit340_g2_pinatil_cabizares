# CSIT340 Group 2 – Full-Stack System & Authentication Module

## Project Description
This repository contains the full-stack implementation of our CSIT340 group project system, featuring a React Vite frontend integrated with a 4-tier Spring Boot REST API backend and MySQL database persistence.

## Tech Stack

### Frontend
- **Framework/Library:** React.js (Vite)
- **Languages:** JavaScript (JSX), CSS3

### Backend
- **Framework:** Spring Boot (Java)
- **Architecture:** 4-Tier System (Controller, Service, Repository, Entity)
- **API Standard:** RESTful Web Services

### Database
- **DBMS:** MySQL
- **ORM / Persistence:** Spring Data JPA / Hibernate

---

## Features Implemented
- **User Authentication:** Registration, login, session restore, and logout
- **Password Security:** BCrypt password hashing, plus one-time upgrade of existing plaintext passwords after a successful login
- **Password Reset:** Single-use, 30-minute reset links sent by email; only token hashes are stored
- **Full-Stack Data Flow:** Authentication forms use the Spring Boot API with an HTTP-only session cookie
- **Persistence:** User records automatically stored in MySQL database
- **CORS Configuration:** Configured cross-origin resource sharing between client and server

---

## Getting Started & Setup

### 1. Database Setup
1. Ensure MySQL Server is installed and running locally.
2. Create a local database schema for the application.

### 2. Backend Setup (Spring Boot)
1. Open the project in **IntelliJ IDEA**.
2. Configure your local database credentials in `src/main/resources/application.properties`.
3. Configure SMTP environment variables so password reset messages can be sent:
   - `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, and `MAIL_PASSWORD`
   - `MAIL_FROM` for the sender address
   - `FRONTEND_URL` for the reset link origin (defaults to `http://localhost:5173`)
4. Set `COOKIE_SECURE=true` when serving the frontend and backend over HTTPS. Leave it false for local HTTP development.
5. Run the main application file (`AppApplication.java`) to start the backend service.

### 3. Frontend Setup (React Vite)
1. Open the project root in **VS Code** or terminal.
2. Install project dependencies:
   ```bash
   npm install
   ```
3. Set `VITE_API_URL` only when the backend is not available on port `8080` at the frontend's hostname.
