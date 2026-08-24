# Mavora

Plataforma SaaS de equipo de marketing autónomo con IA. Monolito modular: backend Java y frontend React.

Estado actual: **Fase 1 — identidad y tenancy**. Registro, sesión en cookie HttpOnly, organización al registrarse, RBAC almacenado y aislamiento entre tenants. Todavía no hay empresa, agentes ni LLM.

## Requisitos

- JDK 21
- Node.js 22
- Docker (PostgreSQL) o PostgreSQL 16 local
- Gradle Wrapper (incluido)

## Arranque local

```bash
docker compose -f infrastructure/docker-compose.yml up -d postgres
cd backend && ./gradlew bootRun
cd frontend && npm install && npm run dev
```

UI: http://localhost:5173  
Crea una cuenta en `/register`. La sesión viaja en cookie `mavora_session` (HttpOnly, SameSite=Lax) a través del proxy de Vite.

Copia `.env.example` a `.env` si necesitas cambiar credenciales. No subas secretos.

## Tests

```bash
cd backend && ./gradlew test
cd frontend && npm run build
```

Los IT usan Testcontainers si Docker está disponible; si no, PostgreSQL en `localhost:5432` (`mavora_test` / `mavora`).

## API

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`
- `GET /api/v1/organizations/{orgId}`
- `GET /api/v1/organizations/{orgId}/members`
- `GET /api/v1/health`
- OpenAPI: `/swagger-ui.html` (`API_DOCS_ENABLED=false` en producción)

Hibernate `ddl-auto=validate`. El esquema solo cambia con Flyway.
