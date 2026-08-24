import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { PageHeader } from "@/components/layout/page-header";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { Panel, StatusBadge } from "@/components/ui/panel";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-provider";
import { decideApproval, fetchApprovals } from "@/lib/api";

export function ApprovalsPage() {
  const { organization } = useAuth();
  const queryClient = useQueryClient();
  const approvals = useQuery({
    queryKey: ["approvals", organization?.id],
    queryFn: ({ signal }) => fetchApprovals(organization!.id, signal),
    enabled: Boolean(organization),
  });
  const decide = useMutation({
    mutationFn: (input: { id: string; approved: boolean }) =>
      decideApproval(organization!.id, input.id, input.approved),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["approvals", organization?.id] });
      void queryClient.invalidateQueries({ queryKey: ["workspace", organization?.id] });
      void queryClient.invalidateQueries({ queryKey: ["content", organization?.id] });
    },
  });

  if (!organization || approvals.isPending) return <Skeleton className="h-64 w-full" />;
  const items = approvals.data?.items ?? [];
  const pending = items.filter((item) => item.status === "PENDING");

  return (
    <div className="space-y-6">
      <PageHeader title="Aprobaciones" description="Nada se ejecuta solo. Estrategia y contenido esperan tu decisión." />
      {pending.length === 0 ? (
        <EmptyState title="Nada pendiente" description="Cuando el CMO o el equipo de contenido propongan, aparecerá aquí." />
      ) : (
        pending.map((item) => (
          <Panel key={item.id} title={item.summary}>
            <div className="space-y-3">
              <StatusBadge value={item.type} />
              <p>{new Date(item.createdAt).toLocaleString("es-ES")}</p>
              <div className="flex gap-2">
                <Button onClick={() => decide.mutate({ id: item.id, approved: true })} disabled={decide.isPending}>
                  Aprobar
                </Button>
                <Button
                  variant="secondary"
                  onClick={() => decide.mutate({ id: item.id, approved: false })}
                  disabled={decide.isPending}
                >
                  Rechazar
                </Button>
              </div>
            </div>
          </Panel>
        ))
      )}
    </div>
  );
}
