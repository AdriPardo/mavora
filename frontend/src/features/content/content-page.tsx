import { useQuery } from "@tanstack/react-query";
import { PageHeader } from "@/components/layout/page-header";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { Panel, StatusBadge } from "@/components/ui/panel";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-provider";
import { fetchContent } from "@/lib/api";
import { useEnqueue, useWorkspace } from "@/lib/workspace";

export function ContentPage() {
  const { organization } = useAuth();
  const workspace = useWorkspace(organization?.id);
  const enqueue = useEnqueue(organization?.id);
  const content = useQuery({
    queryKey: ["content", organization?.id],
    queryFn: ({ signal }) => fetchContent(organization!.id, signal),
    enabled: Boolean(organization),
  });

  if (!organization || content.isPending) return <Skeleton className="h-64 w-full" />;
  const data = content.data;

  return (
    <div className="space-y-6">
      <PageHeader title="Contenido" description="Ideas, piezas y variantes. Las piezas entran en la bandeja de aprobación." />
      {workspace.data?.strategy?.status === "APPROVED" ? (
        <Button onClick={() => enqueue.mutate("CONTENT_CYCLE")} disabled={enqueue.isPending}>
          {enqueue.isPending ? "Generando…" : "Generar ciclo de contenido"}
        </Button>
      ) : (
        <EmptyState title="Sin estrategia aprobada" description="El ciclo de contenido se activa después de la primera estrategia aprobada." />
      )}
      {data && data.ideas.length === 0 && data.pieces.length === 0 ? null : (
        <div className="space-y-3">
          {data?.ideas.map((idea) => (
            <Panel key={idea.id} title={idea.title}>
              <p>{idea.angle}</p>
              {idea.pillar ? <p>Pilar: {idea.pillar}</p> : null}
            </Panel>
          ))}
          {data?.pieces.map((piece) => (
            <Panel key={piece.id} title={piece.title}>
              <StatusBadge value={piece.status} />
              <p className="mt-2">{piece.body}</p>
              <ul className="mt-3 space-y-1">
                {piece.variants.map((variant) => (
                  <li key={variant.id}>
                    <span className="font-medium">{variant.channel}:</span> {variant.body}
                  </li>
                ))}
              </ul>
            </Panel>
          ))}
        </div>
      )}
    </div>
  );
}
