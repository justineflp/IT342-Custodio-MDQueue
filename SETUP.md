# MDQueue – Full Stack Setup

Yes, the frontend is connected to the backend, and the backend is connected to the database. Here’s how it works and what you need to run.

## How It’s Connected

| Layer | What it does |
|-------|-------------------------------|
| **Frontend** (React, port 5173) | Login/Register forms call `/api/auth/login` and `/api/auth/register`. In dev, Vite proxies `/api` to the backend. After login, the JWT is stored and sent on every request to protected routes (e.g. `/api/dashboard`). |
| **Backend** (Spring Boot, port 8080) | Exposes REST API; validates registration (required fields, duplicate email), hashes passwords (BCrypt), issues JWT on login, and protects `/api/dashboard` with the JWT filter. |
| **Database** (MySQL) | User data is stored in `mdqueue_db`. Tables are created/updated automatically on startup (`spring.jpa.hibernate.ddl-auto=update`). |

So: **Frontend → Backend → Database** is wired and usable as long as MySQL and both servers are running.

## Prerequisites

1. **Java 17 (JDK 17)** – Spring Boot 3.5 requires Java 17. If you see `error: release version 17 not supported`, install [Eclipse Temurin 17](https://adoptium.net/temurin/releases/?version=17) or [Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html), then set `JAVA_HOME` to the JDK 17 install folder and use that Java in your PATH.
2. **MySQL** running (e.g. on `localhost:3306`).
3. **Database created**:  
   `CREATE DATABASE mdqueue_db;`
4. **Credentials** in `src/main/resources/application.properties`:  
   - `spring.datasource.username` (default `root`)  
   - `spring.datasource.password` (set if your MySQL user has a password)

## Run the Full Stack

1. **Start the backend** (from project root):
   - **Windows (PowerShell or CMD):** `.\mvnw.cmd spring-boot:run`
   - **macOS/Linux:** `./mvnw spring-boot:run`
   Backend runs at `http://localhost:8080`. On first start, it will create/update the `users` table.

2. **Start the frontend** (in another terminal):
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Frontend runs at `http://localhost:5173`. It will proxy `/api` to the backend.

3. **Use the app**  
   - Open `http://localhost:5173`.  
   - Register a new account (data is saved in MySQL).  
   - Log in (credentials checked against the database).  
   - You’ll be redirected to the dashboard (protected route using the JWT).

## Summary

- **Frontend ↔ Backend**: Connected via Vite proxy in dev and via `@CrossOrigin` on the backend; login/register and dashboard calls hit the Spring Boot API.
- **Backend ↔ Database**: Connected via Spring Data JPA and `application.properties`; registration and login use the MySQL `users` table.

So yes: everything is connected and usable with the database once MySQL is running, the database exists, and the credentials in `application.properties` are correct.

---

## Troubleshooting

### `error: release version 17 not supported`

Your current JDK is older than 17. This project needs **Java 17**.

1. **Check current Java:**  
   `java -version`  
   If it shows something like 1.8 or 11, you need to install and use JDK 17.

2. **Install JDK 17**  
   - [Eclipse Temurin 17 (Adoptium)](https://adoptium.net/temurin/releases/?version=17) – choose Windows x64 MSI, install.  
   - Or [Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html).

3. **Point the project to JDK 17 (Windows)**  
   - Set `JAVA_HOME` to the JDK 17 folder (e.g. `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot`).  
   - In PowerShell (current session):  
     `$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot"`  
     (replace with your actual path).  
   - Then run again: `.\mvnw.cmd spring-boot:run`
