# Mavora

Plataforma SaaS de equipo de marketing autónomo con IA. Monolito modular: backend Java y frontend React.

Estado actual: **Fases 2–9 + Instagram autónomo**. Empresa, objetivo, runtime de agentes, CMO, aprobaciones, research, contenido, social manual, analítica con memoria e Instagram (reels, historias, feed, carruseles) sin aprobación humana. Stripe sigue fuera de este recorte.

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

La generación real usa **DeepSeek** (copy, hooks, prompts) y **Fal.ai** (imágenes Flux Schnell y reels Wan 2.5). Variables: `DEEPSEEK_API_KEY` / `LLM_API_KEY`, `FAL_KEY`. Tests y CI siguen en `LLM_PROVIDER=fake` y `FAL_PROVIDER=fake`. Alternativa OpenAI-compatible: `LLM_PROVIDER=openai`. El tope mensual de la org es duro (`LLM_MONTHLY_BUDGET_CENTS`).

Instagram arranca en `INSTAGRAM_PROVIDER=fake` (cuenta demo, publicación simulada). Para Graph real: app de Meta, cuenta **Professional** (Business/Creator) ligada a una Página, `INSTAGRAM_PROVIDER=meta`, `META_APP_ID`, `META_APP_SECRET`, `INSTAGRAM_REDIRECT_URI` pública y `PUBLIC_API_URL` alcanzable por los servidores de Meta (ellos descargan `image_url`/`video_url`). Los tokens se cifran con `MAVORA_CRYPTO_SECRET` y no se exponen en la API. No hay `scheduled_publish_time` nativo: Mavora guarda el calendario (zona Europe/Madrid) y publica al vencer el slot. El playbook optimiza hacia el algoritmo (hook, mix de formatos, CTA de venta); no promete resultados.

Para enviar la app de Meta a revisión (Ajustes básicos) hacen falta un **icono** (`frontend/public/meta-app-icon.png`, 1024×1024) y una **URL de privacidad pública HTTPS**. La ruta es `/privacidad` (también `/privacidad.html`); localhost no vale. Cuando el frontend esté publicado, pega `https://tu-dominio/privacidad` (o el valor `privacyPolicyUrl` de Integraciones si `PUBLIC_APP_URL` ya es ese dominio). No hay scraping ni login no oficial de Instagram.

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
6. Integraciones → conectar Instagram → brief (fotos opcionales) y «Generar semana»: DeepSeek + Fal.ai, autonomía total, sin bandeja de aprobación

## API (núcleo)

- Auth: `/api/v1/auth/register|login|logout|me`
- Company/goals: `/api/v1/organizations/{id}/company`, `/goals`
- Workspace: `/api/v1/organizations/{id}/workspace`
- Workflows: `POST /api/v1/organizations/{id}/workflows/{type}` (202)
- Strategy, approvals, research, content, publications, analytics, knowledge, agent-runs, usage
- Instagram: `/api/v1/organizations/{id}/instagram`, `/media`, callback OAuth `/api/v1/integrations/instagram/callback`
- Health: `GET /api/v1/health`
- OpenAPI: `/swagger-ui.html` (`API_DOCS_ENABLED=false` en producción)

Hibernate `ddl-auto=validate`. El esquema solo cambia con Flyway.
