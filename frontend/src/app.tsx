import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "@/components/layout/app-shell";
import { RedirectIfAuthenticated, RequireAuth } from "@/features/auth/auth-gates";
import { LoginPage, RegisterPage } from "@/features/auth/auth-pages";
import { OverviewPage } from "@/features/overview/overview-page";
import { PlaceholderPage } from "@/features/placeholders/placeholder-page";
import { SettingsPage } from "@/features/settings/settings-page";

export function App() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <RedirectIfAuthenticated>
            <LoginPage />
          </RedirectIfAuthenticated>
        }
      />
      <Route
        path="/register"
        element={
          <RedirectIfAuthenticated>
            <RegisterPage />
          </RedirectIfAuthenticated>
        }
      />
      <Route element={<RequireAuth />}>
        <Route element={<AppShell />}>
          <Route index element={<Navigate to="/overview" replace />} />
          <Route path="/overview" element={<OverviewPage />} />
          <Route
            path="/strategy"
            element={
              <PlaceholderPage
                title="Estrategia"
                description="Posicionamiento, objetivos y plan del CMO."
                emptyTitle="Todavía no hay estrategia"
                emptyDescription="Cuando la empresa y el objetivo existan, el CMO propondrá una estrategia para aprobar."
              />
            }
          />
          <Route
            path="/content"
            element={
              <PlaceholderPage
                title="Contenido"
                description="Pilares, ideas, piezas y variantes por canal."
                emptyTitle="Sin piezas de contenido"
                emptyDescription="El ciclo de contenido se activará después de la primera estrategia aprobada."
              />
            }
          />
          <Route
            path="/campaigns"
            element={
              <PlaceholderPage
                title="Campañas"
                description="Campañas activas, experimentos y asignación de canales."
                emptyTitle="Sin campañas"
                emptyDescription="Las campañas nacerán del plan de marketing, no como un listado vacío de marketing genérico."
              />
            }
          />
          <Route
            path="/analytics"
            element={
              <PlaceholderPage
                title="Analítica"
                description="Métricas que alimentan decisiones, no solo un dashboard."
                emptyTitle="Sin datos todavía"
                emptyDescription="Cuando haya publicaciones, aquí verás snapshots, insights y aprendizajes."
              />
            }
          />
          <Route
            path="/knowledge"
            element={
              <PlaceholderPage
                title="Knowledge"
                description="Hechos, hipótesis, decisiones y aprendizajes persistentes."
                emptyTitle="Marketing Brain vacío"
                emptyDescription="La memoria del sistema se construye con investigación, ejecución y resultados. No depende del chat."
              />
            }
          />
          <Route
            path="/approvals"
            element={
              <PlaceholderPage
                title="Aprobaciones"
                description="Bandeja de decisiones que requieren a una persona."
                emptyTitle="Nada pendiente"
                emptyDescription="Estrategia, contenido y acciones sensibles aparecerán aquí antes de ejecutarse."
              />
            }
          />
          <Route
            path="/team"
            element={
              <PlaceholderPage
                title="Equipo"
                description="Agentes, actividad, coste y ejecuciones."
                emptyTitle="Equipo inactivo"
                emptyDescription="El CMO será el primer agente. Cada ejecución quedará registrada con modelo, tokens y resultado."
              />
            }
          />
          <Route
            path="/integrations"
            element={
              <PlaceholderPage
                title="Integraciones"
                description="Canales y fuentes de métricas. Contrato listo; adapters después."
                emptyTitle="Sin integraciones"
                emptyDescription="El primer recorte no publica solo. Las credenciales nunca se guardarán en texto plano."
              />
            }
          />
          <Route path="/settings" element={<SettingsPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/overview" replace />} />
    </Routes>
  );
}
