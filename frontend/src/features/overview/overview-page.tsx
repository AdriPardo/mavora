import { Link } from "react-router-dom";
import { PageHeader } from "@/components/layout/page-header";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { Panel, StatusBadge, euros } from "@/components/ui/panel";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-provider";
import { OnboardingForm } from "@/features/overview/onboarding-form";
import { useEnqueue, useWorkspace } from "@/lib/workspace";

export function OverviewPage() {
  const { organization } = useAuth();
  const workspace = useWorkspace(organization?.id);
  const enqueue = useEnqueue(organization?.id);

  if (!organization) {
    return <EmptyState title="Sin organización" description="Vuelve a iniciar sesión." />;
  }
  if (workspace.isPending) {
    return <Skeleton className="h-64 w-full" />;
  }
  if (workspace.isError) {
    return <EmptyState title="No se pudo cargar el overview" description="Comprueba que el backend está en marcha." />;
  }

  const data = workspace.data;
  if (!data.company || !data.goal) {
    return (
      <div>
        <PageHeader
          title="Conecta la empresa"
          description="El equipo de marketing necesita un producto, un mercado y un objetivo medible. No un chat vacío."
        />
        <OnboardingForm organizationId={organization.id} />
      </div>
    );
  }

  return (
    <div>
      <PageHeader
        title="Centro de operaciones"
        description={`${organization.name} · ${organization.role}. Estado del sistema, no un hilo de chat.`}
      />
      <div className="grid gap-3 sm:grid-cols-2">
        <Panel title="Empresa">
          <p className="font-medium text-zinc-900 dark:text-zinc-100">{data.company.name}</p>
          <p>{data.company.market ?? "Mercado sin detallar"}</p>
          {data.company.websiteUrl ? <p className="truncate">{data.company.websiteUrl}</p> : null}
        </Panel>
        <Panel title="Objetivo">
          <p>
            {data.goal.metric}: <span className="font-medium text-zinc-900 dark:text-zinc-100">{data.goal.targetValue}</span>
          </p>
          <p>
            {data.goal.deadline} · {euros(data.goal.budgetCents, data.goal.budgetCurrency)} · {data.goal.market}
          </p>
        </Panel>
        <Panel title="Estrategia">
          {data.strategy ? (
            <div className="space-y-2">
              <StatusBadge value={data.strategy.status} />
              <p>{data.strategy.positioning}</p>
              <Link className="text-zinc-900 underline dark:text-zinc-100" to="/strategy">
                Ver estrategia
              </Link>
            </div>
          ) : (
            <div className="space-y-3">
              <p>Todavía no hay una propuesta del CMO.</p>
              <Button
                onClick={() => enqueue.mutate("CMO_STRATEGY")}
                disabled={enqueue.isPending}
              >
                {enqueue.isPending ? "Lanzando…" : "Pedir estrategia al CMO"}
              </Button>
              {enqueue.error ? <p className="text-red-700 dark:text-red-400">{String(enqueue.error.message)}</p> : null}
            </div>
          )}
        </Panel>
        <Panel title="Aprobaciones pendientes">
          <p className="text-2xl font-semibold text-zinc-900 dark:text-zinc-100">{data.pendingApprovals}</p>
          <Link className="text-zinc-900 underline dark:text-zinc-100" to="/approvals">
            Ir a la bandeja
          </Link>
        </Panel>
        <Panel title="Pipeline de contenido">{data.contentCount} piezas</Panel>
        <Panel title="Campañas">{data.campaignCount} campañas</Panel>
        <Panel title="Instagram">
          <p>Reels, historias, feed y carruseles en piloto automático.</p>
          <Link className="text-zinc-900 underline dark:text-zinc-100" to="/instagram">
            Abrir calendario
          </Link>
        </Panel>
        <Panel title="Knowledge">{data.knowledgeCount} ítems en el Marketing Brain</Panel>
        <Panel title="Uso LLM">
          {euros(data.usage.spentCentsThisMonth, "USD")} de {euros(data.usage.monthlyBudgetCents, "USD")} este mes
        </Panel>
        <Panel title="Actividad de agentes" className="sm:col-span-2">
          {data.runs.length === 0 ? (
            <p>El CMO aún no ha ejecutado tareas.</p>
          ) : (
            <ul className="space-y-2">
              {data.runs.map((run) => (
                <li key={run.id} className="flex flex-wrap items-center gap-2">
                  <StatusBadge value={run.status} />
                  <span>{run.agentType}</span>
                  <span>{run.model ?? "—"}</span>
                  <span>{run.costCents} cénts</span>
                </li>
              ))}
            </ul>
          )}
        </Panel>
      </div>
    </div>
  );
}
