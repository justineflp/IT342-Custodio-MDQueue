# Software Test Plan (STP)

**Project Name:** MDQueue (Medical/Doctor Queue Management System)  
**Course:** IT342 – System Integration and Architecture  
**Student:** Justine Filip Custodio  
**Document Version:** 1.0.0  
**Date:** May 2026  

---

## 1. Introduction

The **MDQueue** system is a multi-platform queue and appointment management application designed for doctors, patients, and administrators. This document details the **Software Test Plan** to validate the system's requirements, specifically verifying that the transition to **Vertical Slice Architecture** has not broken existing functional flows.

This test plan details:
* Functional requirement coverage mapping.
* Detailed test cases (manual steps, inputs, expected outputs).
* Automated integration test specifications.

---

## 2. Functional Requirements Coverage

The following key functional requirements (FR) of the MDQueue system are tracked in this test plan:

| Requirement ID | Module | Description | Target Platform |
|----------------|--------|-------------|-----------------|
| **FR-AUTH-01** | Auth | User Registration (Patient, Doctor, Admin) with password complexity checks. | Web, Mobile, API |
| **FR-AUTH-02** | Auth | User Login (JWT generation and token exchange). | Web, Mobile, API |
| **FR-CLNC-01** | Clinic | Clinic Registration and Details Management. | Web, Mobile, API |
| **FR-QUE-01**  | Queue | Queue Creation for Doctor's clinic. | Web, Mobile, API |
| **FR-QUE-02**  | Queue | Patient joining active queue (Queue Entry creation). | Web, Mobile, API |
| **FR-QUE-03**  | Queue | Queue status updates (Active, Paused, Completed, Cancelled). | Web, Mobile, API |
| **FR-APPT-01** | Appt | Appointment booking and listing. | Web, Mobile, API |
| **FR-APPT-02** | Appt | Medical Document uploading and attachment to appointments. | Web, Mobile, API |
| **FR-PAY-01**  | Pay | Billing generation and payment status updates. | Web, Mobile, API |

---

## 3. Test Environment & Configurations

* **Backend API**: Spring Boot (v3.5.0), PostgreSQL Database (Supabase Cloud).
* **Web Frontend**: React 19, React Router, Vite.
* **Mobile Client**: Android SDK (Min SDK 24, Target SDK 34), Retrofit client, Kotlin MVP.
* **Automated Framework**: JUnit 5, MockMvc, Spring Security Test, AssertJ.

---

## 4. Manual Test Cases (Web & Mobile Clients)

### 4.1. Module: User Authentication (FR-AUTH-01, FR-AUTH-02)

#### Test Case TC-AUTH-01: Secure User Registration
* **Objective**: Verify that a user can successfully register with a strong password, and basic passwords are rejected.
* **Preconditions**: Email is not already registered.
* **Test Steps (Web & Mobile)**:
  1. Open the application and click **Register**.
  2. Enter full name, select role (`PATIENT` or `DOCTOR`), and enter a new email (e.g., `doctor.test@example.com`).
  3. Enter a basic password `123` and click register. Validate rejection.
  4. Enter a strong password `SecurePass123!` and confirm it.
  5. Click **Register**.
* **Expected Outcome**: Rejects basic password. Registers successfully with a strong password. Redirects to `/login`.
* **Verification**: Verify that a row is added to the database `users` table and an event `[AUTH EVENT]` is logged in the backend terminal.

#### Test Case TC-AUTH-02: User Login & JWT Generation
* **Objective**: Verify that users can authenticate and obtain a valid session token.
* **Preconditions**: User `doctor.test@example.com` exists.
* **Test Steps (Web & Mobile)**:
  1. Go to the login page.
  2. Input email `doctor.test@example.com` and password `SecurePass123!`.
  3. Click **Login**.
* **Expected Outcome**: Standardized API response containing a JWT token is returned; the application stores the token and redirects to the **Dashboard**.

---

### 4.2. Module: Clinic & Queue Management (FR-CLNC-01, FR-QUE-01, FR-QUE-03)

#### Test Case TC-QUE-01: Create and Activate Daily Queue
* **Objective**: Verify that a Doctor can create and activate a new queue for their clinic.
* **Preconditions**: User is logged in as a `DOCTOR`. A clinic is associated with the doctor.
* **Test Steps (Web/Mobile/API)**:
  1. Navigate to the **Clinics** page.
  2. Click **Create Queue**.
  3. Fill in queue name (e.g., "Morning General Consultations"), specify max capacity (e.g., `30`), and select active status.
  4. Click **Submit**.
* **Expected Outcome**: Queue is created successfully, labeled with `ACTIVE` status, and appears on the Dashboard.

#### Test Case TC-QUE-02: Update Queue Status
* **Objective**: Verify that a doctor can pause or complete an active queue.
* **Preconditions**: An `ACTIVE` queue exists.
* **Test Steps (Mobile/Web)**:
  1. From the Dashboard, view the active queue list.
  2. Select the queue and click **Pause Queue** (or update status to `PAUSED`).
  3. Verify status changes on UI.
* **Expected Outcome**: Status updates successfully to `PAUSED` in real time, stopping new patients from joining.

---

### 4.3. Module: Appointment Booking & Document Management (FR-APPT-01, FR-APPT-02)

#### Test Case TC-APPT-01: Book Appointment & Upload Document
* **Objective**: Verify that a patient can book an appointment and upload a medical history PDF.
* **Preconditions**: Logged in as a `PATIENT`. An active doctor clinic is available.
* **Test Steps (Web & Mobile)**:
  1. Navigate to **Book Appointment**.
  2. Select Doctor, date, and input appointment notes.
  3. Under "Attach Medical Document", select a local test file (e.g., `health_record.pdf`).
  4. Click **Confirm Appointment**.
* **Expected Outcome**: Appointment is created with status `PENDING`. The PDF document is uploaded to the backend and associated with the appointment.

---

## 5. Automated Integration Test Cases (Regression Suite)

Automated tests are developed on the Backend API using Spring's `MockMvc` to guarantee core feature regressions are caught during refactoring.

### 5.1. Authentication Regression Test (`AuthControllerTest.java`)
* **Test Registration Integrity**: Sends registration payload to `/api/auth/register`. Asserts standard response containing success messages and adapter mapping checks.
* **Test Password Validation Policy**: Checks that registering with passwords violating the `PasswordValidationStrategy` returns `400 Bad Request` with appropriate validation errors.
* **Test Login and JWT Generation**: Sends POST to `/api/auth/login`. Asserts a `200 OK` status and verifies the presence of `"token"` and `"type": "Bearer"` inside the standardized `"data"` block.

### 5.2. Queue & Entry Management Regression Test (`QueueControllerTest.java`)
* **Test Create Queue for Clinic**: Performs authenticated POST request to `/api/queues/clinic/{clinicId}`. Verifies that the queue is generated and belongs to the authenticated doctor.
* **Test Fetch Queues**: Performs GET to `/api/queues/clinic/{clinicId}`. Verifies that the response contains the list of queues in standard API response format.
* **Test State Transitions**: Performs PATCH to `/api/queues/{id}/status`. Asserts that updating status to `COMPLETED` modifies database state.

---

## 6. Execution and Reporting Strategy

1. **Automated Suite**: Run via Maven `.\mvnw.cmd test`.
2. **Manual Regression**: Run local servers for both frontend and backend, launch the Android emulator, and perform end-to-end verification.
3. **Evidence Recording**: Test outputs, compiler outputs, and manual results are compiled into the **Full Regression Test Report**.
