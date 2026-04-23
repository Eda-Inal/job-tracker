# Job Tracker API

A REST API for tracking job applications end-to-end. Users can create and manage applications, track interview schedules, filter and search their history, and receive automated email reminders — with an AI assistant layer planned for natural-language queries over their own data.

> **Status:** Active development — core authentication and job application management are complete.

---

## Features

### Implemented
- JWT authentication (register, login, access/refresh token rotation, logout)
- Full job application CRUD with pagination and sorting
- Soft delete with trash and restore flow
- Dynamic filtering: keyword search (company / position), status, date range, location
- User-scoped data access — users can only see and modify their own records

### Planned
- Interview management (scheduling, outcomes, upcoming list)
- Dashboard & analytics (status distribution, monthly trend, response rate, heatmap)
- Email notifications (interview reminders, weekly summaries) via scheduled jobs
- In-app notifications
- Audit logging with AOP
- Rate limiting
- AI chatbot for querying your own application data

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Security | Spring Security + JWT (jjwt) |
| Persistence | Spring Data JPA / Hibernate 6 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Mapping | MapStruct |
| Validation | Jakarta Validation |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Caching | Spring Cache + Caffeine *(planned)* |
| Email | Spring Mail + Thymeleaf *(planned)* |
| AI | Spring AI *(planned)* |
| Rate Limiting | Bucket4j *(planned)* |
| Testing | JUnit 5, Mockito, H2 (integration) |
| DevOps | Docker, Docker Compose |

---

## Getting Started

### Prerequisites
- Java 21
- Docker & Docker Compose
- Maven

### 1. Start infrastructure

```bash
docker compose -f docker/docker-compose.yml up -d
```

This starts:
- **PostgreSQL 16** on `localhost:5433`
- **MailHog** (dev mail catcher) on `localhost:1025` — UI at `http://localhost:8025`

### 2. Run the application

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` with the `dev` profile active. Flyway runs migrations automatically on startup.

### 3. Verify

```
GET http://localhost:8080/actuator/health
```

---

## API

Swagger UI is available at:
```
http://localhost:8080/swagger-ui.html
```

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Create account |
| POST | `/auth/login` | Login, returns access + refresh token |
| POST | `/auth/refresh` | Rotate access token |
| POST | `/auth/logout` | Revoke refresh token |

### Job Applications

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/applications` | List with pagination, sorting, and filters |
| POST | `/applications` | Create application |
| GET | `/applications/{id}` | Get by ID |
| PUT | `/applications/{id}` | Full update |
| PATCH | `/applications/{id}` | Partial update |
| DELETE | `/applications/{id}` | Soft delete |
| GET | `/applications/trash` | List soft-deleted |
| POST | `/applications/{id}/restore` | Restore from trash |

#### Query parameters for `GET /applications`

| Parameter | Type | Description |
|-----------|------|-------------|
| `search` | string | Searches company name and position (case-insensitive) |
| `status` | enum | `APPLIED` `INTERVIEWING` `OFFER` `REJECTED` `ACCEPTED` `WITHDRAWN` |
| `startDate` | date (ISO) | Application date from (inclusive) |
| `endDate` | date (ISO) | Application date to (inclusive) |
| `location` | string | Partial match on location |
| `page` | int | Page number, default `0` |
| `size` | int | Page size, default `20` |
| `sort` | string | e.g. `applicationDate,desc` |

---

## Project Structure

```
src/main/java/com/jobtracker/
├── auth/               # JWT auth, filters, refresh token
├── user/               # User entity, profile management
├── application/        # Job application CRUD, soft delete, filtering
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── mapper/
│   └── specification/
├── common/             # ApiResponse, PageResponse, exceptions
└── config/             # Security, CORS, cache config
```

---

## Environment Variables

For production, override these via environment variables or a secrets manager:

| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `APP_JWT_SECRET` | Base64-encoded JWT signing key (min 256 bit) |
| `APP_JWT_ACCESS_TOKEN_EXPIRATION` | Access token TTL in ms |
| `APP_JWT_REFRESH_TOKEN_EXPIRATION` | Refresh token TTL in ms |

---

## Running Tests

```bash
./mvnw test
```

Unit tests use Mockito. Integration tests use an in-memory H2 database — no Docker required.
