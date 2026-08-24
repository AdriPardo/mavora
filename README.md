# Mavora

Plataforma SaaS de equipo de marketing autónomo con IA. Este repositorio es un monolito modular: backend Java y frontend React en el mismo repo.

Estado actual: **Fase 0 — cimientos**. Arranca, health-check, shell de producto y tests de arquitectura. Todavía no hay registro, agentes ni LLM.

## Requisitos

- JDK 21
- Node.js 22
- Docker (PostgreSQL) o PostgreSQL 16 local
- Gradle Wrapper (incluido)

## Arranque local

```bash
# 1. Base de datos
docker compose -f infrastructure/docker-compose.yml up -d postgres

# 2. Backend  (http://localhost:8080)
cd backend
./gradlew bootRun

# 3. Frontend (http://localhost:5173)
cd frontend
npm install
npm run dev
```

Copia `.env.example` a `.env` si necesitas cambiar credenciales. No subas secretos.

La SPA hace proxy de `/api` y `/actuator` al backend. Comprueba el estado en **Ajustes**.

## Tests

```bash
cd backend
./gradlew test
```

Los tests de integración usan Testcontainers si Docker está disponible. Si no, esperan PostgreSQL en `localhost:5432` (`mavora_test` / usuario `mavora`).

```bash
cd frontend
npm run build
```

## API

- `GET /api/v1/health` — liveness de aplicación
- `GET /actuator/health` — probes
- `/swagger-ui.html` — OpenAPI (desactivar en producción con `API_DOCS_ENABLED=false`)

Hibernate `ddl-auto` está en `validate`. El esquema solo cambia con Flyway.

## Estructura

```
backend/          Spring Boot 3.5, Java 21, Gradle
frontend/         React + TypeScript + Vite
infrastructure/   Docker Compose (Postgres)
```
