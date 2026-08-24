import { PageHeader } from "@/components/layout/page-header";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { Panel, StatusBadge, parseJsonList } from "@/components/ui/panel";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-provider";
import { useEnqueue, useWorkspace } from "@/lib/workspace";

export function StrategyPage() {
  const { organization } = useAuth();
  const workspace = useWorkspace(organization?.id);
  const enqueue = useEnqueue(organization?.id);

  if (!organization || workspace.isPending) return <Skeleton className="h-64 w-full" />;
  const strategy = workspace.data?.strategy;
  return (
    <div className="space-y-6">
      <PageHeader title="Estrategia" description="Posicionamiento, ICP y plan del CMO. Se ejecuta cuando lo apruebas." />
      {!workspace.data?.company || !workspace.data.goal ? (
        <EmptyState title="Falta contexto" description="Conecta empresa y objetivo en Overview antes de pedir una estrategia." />
      ) : !strategy ? (
        <div className="space-y-3">
          <EmptyState title="Todavía no hay estrategia" description="El CMO propondrá un plan a partir de la empresa y el objetivo." />
          <Button onClick={() => enqueue.mutate("CMO_STRATEGY")} disabled={enqueue.isPending}>
            {enqueue.isPending ? "Lanzando…" : "Pedir estrategia al CMO"}
          </Button>
        </div>
      ) : (
        <div className="space-y-3">
          <StatusBadge value={strategy.status} />
          <Panel title="Posicionamiento">{strategy.positioning}</Panel>
          <Panel title="ICP">{strategy.icpSummary}</Panel>
          <Panel title="Canales">{parseJsonList(strategy.channelsJson).join(" · ") || strategy.channelsJson}</Panel>
          <Panel title="Pilares">{parseJsonList(strategy.pillarsJson).join(" · ") || strategy.pillarsJson}</Panel>
          <Panel title="Narrativa">{strategy.narrative}</Panel>
          {strategy.status !== "DRAFT" ? (
            <Button variant="secondary" onClick={() => enqueue.mutate("CMO_STRATEGY")} disabled={enqueue.isPending}>
              Pedir una nueva propuesta
            </Button>
          ) : null}
        </div>
      )}
    </div>
  );
}
