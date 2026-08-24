import { useQuery } from "@tanstack/react-query";
import { PageHeader } from "@/components/layout/page-header";
import { EmptyState } from "@/components/ui/empty-state";
import { Panel, StatusBadge } from "@/components/ui/panel";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-provider";
import { fetchKnowledge } from "@/lib/api";

export function KnowledgePage() {
  const { organization } = useAuth();
  const knowledge = useQuery({
    queryKey: ["knowledge", organization?.id],
    queryFn: ({ signal }) => fetchKnowledge(organization!.id, signal),
    enabled: Boolean(organization),
  });

  if (!organization || knowledge.isPending) return <Skeleton className="h-64 w-full" />;
  const items = knowledge.data?.items ?? [];

  return (
    <div className="space-y-6">
      <PageHeader title="Knowledge" description="Hechos, hipótesis, decisiones y aprendizajes persistentes. No es el contexto de un chat." />
      {items.length === 0 ? (
        <EmptyState title="Marketing Brain vacío" description="La memoria se construye con website, investigación, aprobaciones y resultados." />
      ) : (
        items.map((item) => (
          <Panel key={item.id} title={item.title}>
            <StatusBadge value={item.kind} />
            <p className="mt-2">{item.body}</p>
            {item.source ? <p className="mt-1 truncate text-xs">{item.source}</p> : null}
          </Panel>
        ))
      )}
    </div>
  );
}
