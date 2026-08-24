# Mavora

Plataforma SaaS de equipo de marketing autónomo con IA. Monolito modular: backend Java y frontend React.

Estado actual: **Fases 2–9**. Empresa, objetivo, runtime de agentes, CMO, aprobaciones, research, contenido, social manual y analítica con memoria. Integraciones reales y Stripe quedan fuera de este recorte.

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

Por defecto el LLM es un proveedor **fake** determinista (`LLM_PROVIDER=fake`). Para un endpoint OpenAI-compatible: `LLM_PROVIDER=openai`, `LLM_BASE_URL`, `LLM_API_KEY`, `LLM_MODEL_CMO`. El tope mensual de la org es duro (`LLM_MONTHLY_BUDGET_CENTS`).

## Tests

```bash
cd backend && ./gradlew test
cd frontend && npm run build
```

Los IT usan Testcontainers si Docker está disponible; si no, PostgreSQL en `localhost:5432` (`mavora_test` / `mavora`).

## Recorrido

1. Registro (crea usuario + organización)
2. Overview: empresa, producto, website opcional y objetivo (métrica, target, fecha, presupuesto en céntimos)
3. Pedir estrategia al CMO → DRAFT + aprobación
4. Aprobar → campaña + FACT/DECISION en knowledge
5. Research, contenido (con aprobación), plan social (publicación manual) y analítica (snapshots → insight → learning)

## API (núcleo)

- Auth: `/api/v1/auth/register|login|logout|me`
- Company/goals: `/api/v1/organizations/{id}/company`, `/goals`
- Workspace: `/api/v1/organizations/{id}/workspace`
- Workflows: `POST /api/v1/organizations/{id}/workflows/{type}` (202)
- Strategy, approvals, research, content, publications, analytics, knowledge, agent-runs, usage
- Health: `GET /api/v1/health`
- OpenAPI: `/swagger-ui.html` (`API_DOCS_ENABLED=false` en producción)

Hibernate `ddl-auto=validate`. El esquema solo cambia con Flyway.
