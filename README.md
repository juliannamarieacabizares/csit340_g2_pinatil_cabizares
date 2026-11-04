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
- **User Authentication:** Registration, Login, and Forgot Password UI components
- **Full-Stack Data Flow:** Registration form posts directly to Spring Boot backend API
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
3. Run the main application file (`AppApplication.java`) to start the backend service.

### 3. Frontend Setup (React Vite)
1. Open the project root in **VS Code** or terminal.
2. Install project dependencies:
   ```bash
   npm install
