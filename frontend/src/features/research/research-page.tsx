import { useQuery } from "@tanstack/react-query";
import { PageHeader } from "@/components/layout/page-header";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { Panel, parseJsonList } from "@/components/ui/panel";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-provider";
import { fetchResearch } from "@/lib/api";
import { useEnqueue, useWorkspace } from "@/lib/workspace";

export function ResearchPage() {
  const { organization } = useAuth();
  const workspace = useWorkspace(organization?.id);
  const enqueue = useEnqueue(organization?.id);
  const research = useQuery({
    queryKey: ["research", organization?.id],
    queryFn: ({ signal }) => fetchResearch(organization!.id, signal),
    enabled: Boolean(organization),
  });

  if (!organization || research.isPending) return <Skeleton className="h-64 w-full" />;
  const data = research.data;
  const empty = !data || (data.personas.length === 0 && data.competitors.length === 0);

  return (
    <div className="space-y-6">
      <PageHeader title="Investigación" description="Personas, competidores e ICP persistidos en el Marketing Brain." />
      {workspace.data?.strategy?.status === "APPROVED" ? (
        <Button onClick={() => enqueue.mutate("MARKET_RESEARCH")} disabled={enqueue.isPending}>
          {enqueue.isPending ? "Investigando…" : "Lanzar research"}
        </Button>
      ) : (
        <EmptyState title="Estrategia no aprobada" description="Aprueba una estrategia para que el researcher tenga un marco." />
      )}
      {empty ? null : (
        <div className="grid gap-3 lg:grid-cols-2">
          {data.icp ? <Panel title="ICP">{data.icp.summary}</Panel> : null}
          {data.icp ? <Panel title="Segmentos">{parseJsonList(data.icp.segmentsJson).join(" · ")}</Panel> : null}
          {data.personas.map((persona) => (
            <Panel key={persona.id} title={persona.name}>
              <p>{persona.summary}</p>
              <p>Pains: {persona.pains}</p>
              <p>Jobs: {persona.jobs}</p>
            </Panel>
          ))}
          {data.competitors.map((competitor) => (
            <Panel key={competitor.id} title={competitor.name}>
              <p>{competitor.notes}</p>
              {competitor.url ? <p className="truncate">{competitor.url}</p> : null}
            </Panel>
          ))}
        </div>
      )}
    </div>
  );
}
