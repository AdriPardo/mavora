import { EmptyState } from "@/components/ui/empty-state";
import { PageHeader } from "@/components/layout/page-header";

const PANELS = [
  {
    title: "Objetivo",
    body: "Aún no hay un objetivo de marketing. Se definirá al conectar la empresa.",
  },
  {
    title: "Progreso y KPIs",
    body: "Las métricas aparecerán cuando existan campañas y mediciones.",
  },
  {
    title: "Campañas y experimentos",
    body: "No hay campañas ni experimentos activos.",
  },
  {
    title: "Pipeline de contenido",
    body: "Las piezas y su estado de aprobación se mostrarán aquí.",
  },
  {
    title: "Aprobaciones pendientes",
    body: "Nada espera tu decisión.",
  },
  {
    title: "Actividad de agentes",
    body: "El CMO y el resto del equipo virtual aún no han ejecutado tareas.",
  },
  {
    title: "Aprendizajes",
    body: "El Marketing Brain se construirá con resultados reales.",
  },
  {
    title: "Acciones recomendadas",
    body: "Cuando haya contexto de empresa, aquí verás el siguiente paso concreto.",
  },
] as const;

export function OverviewPage() {
  return (
    <div>
      <PageHeader
        title="Centro de operaciones"
        description="Vista operativa de tu equipo de marketing. No es un chat: es el estado del sistema."
      />
      <div className="grid gap-3 sm:grid-cols-2">
        {PANELS.map((panel) => (
          <EmptyState key={panel.title} title={panel.title} description={panel.body} className="min-h-36" />
        ))}
      </div>
    </div>
  );
}
