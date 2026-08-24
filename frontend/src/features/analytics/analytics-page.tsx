import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { PageHeader } from "@/components/layout/page-header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { EmptyState } from "@/components/ui/empty-state";
import { Panel } from "@/components/ui/panel";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-provider";
import { fetchAnalytics, recordSnapshot } from "@/lib/api";
import { useEnqueue } from "@/lib/workspace";

export function AnalyticsPage() {
  const { organization } = useAuth();
  const queryClient = useQueryClient();
  const enqueue = useEnqueue(organization?.id);
  const [metric, setMetric] = useState("signups");
  const [value, setValue] = useState("12");
  const analytics = useQuery({
    queryKey: ["analytics", organization?.id],
    queryFn: ({ signal }) => fetchAnalytics(organization!.id, signal),
    enabled: Boolean(organization),
  });
  const record = useMutation({
    mutationFn: () => recordSnapshot(organization!.id, { metric, value: Number(value), source: "manual" }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["analytics", organization?.id] });
    },
  });

  if (!organization || analytics.isPending) return <Skeleton className="h-64 w-full" />;
  const data = analytics.data;

  return (
    <div className="space-y-6">
      <PageHeader title="Analítica" description="Snapshots que alimentan insights y aprendizajes. No es un dashboard vacío." />
      <form
        className="flex flex-wrap items-end gap-3"
        onSubmit={(event) => {
          event.preventDefault();
          record.mutate();
        }}
      >
        <label className="space-y-1.5">
          <span className="text-sm font-medium">Métrica</span>
          <Input value={metric} onChange={(event) => setMetric(event.target.value)} />
        </label>
        <label className="space-y-1.5">
          <span className="text-sm font-medium">Valor</span>
          <Input type="number" value={value} onChange={(event) => setValue(event.target.value)} />
        </label>
        <Button type="submit" disabled={record.isPending}>
          Registrar snapshot
        </Button>
        <Button type="button" variant="secondary" onClick={() => enqueue.mutate("ANALYTICS_CYCLE")} disabled={enqueue.isPending}>
          Generar insights
        </Button>
      </form>
      {!data || (data.snapshots.length === 0 && data.insights.length === 0) ? (
        <EmptyState title="Sin datos todavía" description="Registra un snapshot y lanza el ciclo de analítica." />
      ) : (
        <div className="grid gap-3 lg:grid-cols-2">
          <Panel title="Snapshots">
            <ul className="space-y-1">
              {data.snapshots.map((item) => (
                <li key={item.id}>
                  {item.metric}: {item.value} ({item.source})
                </li>
              ))}
            </ul>
          </Panel>
          {data.insights.map((item) => (
            <Panel key={item.id} title={item.title}>
              {item.body}
            </Panel>
          ))}
          {data.learnings.map((item) => (
            <Panel key={item.id} title={item.title}>
              {item.body}
            </Panel>
          ))}
        </div>
      )}
    </div>
  );
}
