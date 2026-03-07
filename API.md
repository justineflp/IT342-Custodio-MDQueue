# MDQueue Authentication API

## Overview

REST API for user registration and login with JWT-based authentication.

## Base URL

```
http://localhost:8080/api
```

## Endpoints

### 1. User Registration

**POST** `/api/auth/register`

Creates a new user account. Validates required fields and prevents duplicate email registration.

**Request Body:**
```json
{
  "fullName": "John Doe",
  "email": "you@example.com",
  "phoneNumber": "+1 (555) 000-0000",
  "password": "yourSecurePassword123",
  "confirmPassword": "yourSecurePassword123"
}
```

**Required Fields:**
- `fullName` (string, max 100 chars)
- `email` (valid email format)
- `password` (min 8 characters)
- `confirmPassword` (must match password)

**Optional:**
- `phoneNumber` (string, max 20 chars)

**Success Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "you@example.com",
  "fullName": "John Doe",
  "message": "Account created successfully"
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors (missing/invalid fields)
- `409 Conflict` - Email already registered

---

### 2. User Login

**POST** `/api/auth/login`

Authenticates a user and returns a JWT token.

**Request Body:**
```json
{
  "email": "you@example.com",
  "password": "yourSecurePassword123"
}
```

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "you@example.com",
  "fullName": "John Doe",
  "message": "Login successful"
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors
- `401 Unauthorized` - Invalid email or password

---

### 3. Dashboard (Protected)

**GET** `/api/dashboard`

Returns dashboard data. Requires valid JWT in `Authorization` header.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Success Response (200 OK):**
```json
{
  "message": "Welcome to your dashboard",
  "email": "you@example.com"
}
```

---

### 4. Home (Protected)

**GET** `/api/home`

Returns welcome message. Requires valid JWT for authenticated content.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

---

## Using the Token

Include the JWT in the `Authorization` header for protected endpoints:

```
Authorization: Bearer <token>
```

## Database Setup

1. Create MySQL database: `CREATE DATABASE mdqueue_db;`
2. Update `application.properties` with your MySQL credentials
3. Tables are auto-created on startup (`spring.jpa.hibernate.ddl-auto=update`)
