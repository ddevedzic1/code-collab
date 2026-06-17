# CodeCollab

CodeCollab is a web platform for writing, running, and sharing code directly in the
browser — no local development environment required. Users write code in an in-browser
editor, execute it inside an isolated Docker sandbox, and share their snippets with
others through a generated link (read-only or edit access).

## Features

- User registration, login/logout, and account management (session-based authentication)
- Create, edit, delete, search, and paginate code snippets
- Run code in an isolated Docker container and poll for the result
- Execution history per snippet
- Share snippets via a public link or with specific users, in read-only or edit mode

## Architecture

CodeCollab is built as a set of Spring Boot microservices behind an API gateway, with
service discovery, synchronous and asynchronous inter-service communication, and a
single-page React frontend.

| Component | Description | Port |
|-----------|-------------|------|
| `eureka-service` | Service discovery (Eureka) | 8761 |
| `api-gateway` | Single entry point; authenticates requests and routes them | 8080 |
| `user-service` | Accounts, authentication, session management | 8081 |
| `snippet-service` | Snippets, languages, and sharing | 8082 |
| `execution-service` | Code execution and execution history | 8083 |
| `system-events-service` | Audit / system event log | 8084 |
| `frontend` | React single-page application | 8090 |

Supporting infrastructure: **PostgreSQL** (one database per service) and **RabbitMQ**
for asynchronous messaging.

## Tech stack

- **Backend:** Java 21, Spring Boot 3.5, Spring Cloud 2025 (Gateway, Eureka, OpenFeign,
  LoadBalancer, Resilience4j)
- **Database:** PostgreSQL with Flyway migrations
- **Messaging:** RabbitMQ
- **Code execution:** Docker
- **Frontend:** React + TypeScript (Vite, Chakra UI, CodeMirror)
- **Build:** Maven (backend), npm (frontend)

## Prerequisites

- [Docker](https://www.docker.com/) and Docker Compose
- For local (non-Docker) development:
  - JDK 21
  - Maven 3.9+
  - Node.js 20+
  - A running PostgreSQL and RabbitMQ instance

## Running with Docker Compose (recommended)

From the project root:

```bash
docker compose up --build -d
```

This builds and starts all services, the gateway, Eureka, PostgreSQL, RabbitMQ, and the
frontend. Once everything reports healthy:

- Frontend: <http://localhost:8090>
- API gateway: <http://localhost:8080>
- Eureka dashboard: <http://localhost:8761>
- RabbitMQ management: <http://localhost:15672>

Stop everything with:

```bash
docker compose down
```

> The `execution-service` mounts the host Docker socket so it can launch sandbox
> containers for code execution. Docker must be running on the host.

### Configuration

Credentials and connection settings are provided through environment variables with
sensible defaults (see `docker-compose.yml`). You can override them via a `.env` file or
your shell, for example:

```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
```

## Running locally (without Docker)

Start PostgreSQL and RabbitMQ, then run each service from its own directory:

```bash
cd <service-directory>
./mvnw spring-boot:run
```

Start `eureka-service` first, then the business services and the gateway. For the
frontend:

```bash
cd frontend
npm install
npm run dev
```

The frontend dev server runs on <http://localhost:5173>.

## Running tests

Run the test suite for any service with Maven:

```bash
cd <service-directory>
./mvnw test
```

## Project structure

```
code-collab/
├── eureka-service/          # Service discovery
├── api-gateway/             # Edge gateway and authentication
├── user-service/            # Accounts and authentication
├── snippet-service/         # Snippets, languages, sharing
├── execution-service/       # Code execution and history
├── system-events-service/   # Audit / system events
├── frontend/                # React single-page application
└── docker-compose.yml       # Full local orchestration
```
