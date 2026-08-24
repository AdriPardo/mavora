import { useQuery } from "@tanstack/react-query";
import { PageHeader } from "@/components/layout/page-header";
import { EmptyState } from "@/components/ui/empty-state";
import { Panel, StatusBadge, euros } from "@/components/ui/panel";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-provider";
import { fetchAgentRuns, fetchUsage } from "@/lib/api";

export function TeamPage() {
  const { organization } = useAuth();
  const runs = useQuery({
    queryKey: ["runs", organization?.id],
    queryFn: ({ signal }) => fetchAgentRuns(organization!.id, signal),
    enabled: Boolean(organization),
  });
  const usage = useQuery({
    queryKey: ["usage", organization?.id],
    queryFn: ({ signal }) => fetchUsage(organization!.id, signal),
    enabled: Boolean(organization),
  });

  if (!organization || runs.isPending || usage.isPending) return <Skeleton className="h-64 w-full" />;
  const items = runs.data?.items ?? [];

  return (
    <div className="space-y-6">
      <PageHeader title="Equipo" description="Agentes, modelo, tokens y coste. El CMO es el primero." />
      {usage.data ? (
        <Panel title="Presupuesto LLM del mes">
          {euros(usage.data.spentCentsThisMonth, "USD")} de {euros(usage.data.monthlyBudgetCents, "USD")}
        </Panel>
      ) : null}
      {items.length === 0 ? (
        <EmptyState title="Equipo inactivo" description="Cada ejecución quedará registrada con modelo, tokens y resultado." />
      ) : (
        items.map((run) => (
          <Panel key={run.id} title={run.agentType}>
            <div className="flex flex-wrap gap-2">
              <StatusBadge value={run.status} />
              <span>{run.model ?? "—"}</span>
              <span>
                {run.promptTokens}+{run.completionTokens} tokens
              </span>
              <span>{run.costCents} cénts</span>
            </div>
            {run.errorMessage ? <p className="mt-2 text-red-700 dark:text-red-400">{run.errorMessage}</p> : null}
          </Panel>
        ))
      )}
    </div>
  );
}
