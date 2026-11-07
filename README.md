# CSIT340 Group 2 – Full-Stack System & Authentication Module

## Project Description

This repository contains the full-stack implementation of our CSIT340 group project system. It uses a React Vite frontend, a 4-tier Spring Boot REST API backend, and MySQL for data storage.

The project includes a complete authentication module (registration, login, password reset, user roles) and a reusable CRUD page. The CRUD page uses a placeholder entity called `Item`. We will rename it when our system proposal is approved.

## Tech Stack

### Frontend

* **Framework/Library:** React.js (Vite)
* **Routing:** React Router
* **Languages:** JavaScript (JSX), CSS3

### Backend

* **Framework:** Spring Boot (Java 17)
* **Architecture:** 4-Tier System (Controller, Service, Repository, Entity)
* **API Standard:** RESTful Web Services

### Database

* **DBMS:** MySQL
* **ORM / Persistence:** Spring Data JPA / Hibernate

## Features Implemented

* **User Authentication:** Registration, login, session restore after a page refresh, and logout
* **Password Security:** BCrypt password hashing, plus a one-time upgrade of existing plaintext passwords after a successful login
* **Password Reset:** Single-use, 30-minute reset links sent by email. Only token hashes are stored.
* **User Roles:** Two roles, `USER` and `ADMIN`. The role is returned by `/api/auth/me` and used to show or hide pages.
* **API Protection:** An interceptor in Spring Boot requires a login for every `/api/**` endpoint, except the login, register and password reset endpoints. Endpoints under `/api/admin/**` also require the `ADMIN` role.
* **Frontend Routing:** React Router with protected routes, a redirect back to the wanted page after login, a dashboard layout with a navbar, and a 404 page
* **Items CRUD Page:** Add, edit and delete with a confirm window, search by name or category, and pagination
* **Full-Stack Data Flow:** The frontend uses the Spring Boot API with an HTTP-only session cookie
* **Persistence:** All records are stored in the MySQL database
* **CORS Configuration:** Cross-origin resource sharing is configured between client and server

## Frontend Routes

| Path | Who can open it |
| --- | --- |
| `/login`, `/register` | Logged-out users only |
| `/forgot-password`, `/reset-password` | Everyone |
| `/dashboard`, `/items` | Logged-in users |
| `/admin` | Admins only |

## API Endpoints

| Method and path | Access |
| --- | --- |
| `POST /api/users/register` | Public |
| `POST /api/auth/login`, `POST /api/auth/logout` | Public |
| `GET /api/auth/me` | Logged-in session |
| `POST /api/auth/password-reset/request` | Public |
| `POST /api/auth/password-reset/complete` | Public |
| `GET /api/items?search=&page=&size=` | Logged-in users |
| `POST /api/items` | Logged-in users |
| `PUT /api/items/{id}`, `DELETE /api/items/{id}` | Logged-in users |
| `GET /api/admin/ping` | Admins only |

## Getting Started & Setup

### Requirements

* JDK 17 or higher
* MySQL Server
* Node.js 20.19 or higher (or 22.12 or higher)

### 1. Database Setup

1. Make sure MySQL Server is installed and running locally.
2. Create a local database schema for the application.
3. Spring Boot creates the tables automatically when it starts (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Backend Setup (Spring Boot)

1. Open the project in IntelliJ IDEA.
2. Set your local database credentials in `src/main/resources/application.properties`.
3. Set these environment variables so password reset emails can be sent:
   * `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, and `MAIL_PASSWORD`
   * `MAIL_FROM` for the sender address
   * `FRONTEND_URL` for the reset link origin (defaults to `http://localhost:5173`)
4. Set `COOKIE_SECURE=true` when the frontend and backend run over HTTPS. Leave it false for local HTTP development.
5. Run the main application file (`AppApplication.java`) to start the backend on port 8080.

### 3. Frontend Setup (React Vite)

1. Open the project root in VS Code or a terminal.
2. Install the dependencies:

```
npm install
```

3. Start the development server:

```
npm run dev
```

4. Open http://localhost:5173.
5. Optional: if the backend is not on port `8080` of the same host, copy `.env.example` to a new file named `.env` in the project root (next to `package.json`), then edit the value:

```
VITE_API_URL=http://localhost:8080
```

Do not add `/api` to the value. Restart `npm run dev` after you change `.env`. The `.env` file is not committed to git, but `.env.example` is, so everyone can see which settings exist.

### 4. Create an Admin Account

New accounts get the `USER` role. To make an account an admin:

1. Register the account in the app.
2. Run this in MySQL Workbench:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'your@email.com';
```

3. Reload the app. The Admin link appears in the navbar.

## Project Structure

```
src/
├── main/java/com/cituccs/sims/    Spring Boot backend
│   ├── config/                    Password encoder, CORS, AuthInterceptor
│   ├── controller/                REST controllers (Auth, User, Item, Admin)
│   ├── service/                   Business logic
│   ├── repository/                Spring Data JPA repositories
│   ├── entity/                    JPA entities (User, Role, Item, PasswordResetToken)
│   └── web/                       Request and response models
├── components/                    Reusable React components
├── context/                       AuthProvider (logged-in user state)
├── hooks/                         useAuth
├── pages/                         Route pages (Login, Dashboard, Items, Admin, ...)
└── authApi.js                     All calls to the backend API
```
