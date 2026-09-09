import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { PageHeader } from "@/components/layout/page-header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { EmptyState } from "@/components/ui/empty-state";
import { Panel } from "@/components/ui/panel";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-provider";
import { fetchAnalytics, recordSnapshot } from "@/lib/api";
import { useEnqueue } from "@/lib/workspace";

const KPI_CHIPS: { id: string; label: string; hint: string }[] = [
  { id: "alcance", label: "Alcance", hint: "Cuentas únicas que vieron el contenido (Instagram Insights)." },
  { id: "seguidores", label: "Seguidores", hint: "Total de seguidores en el momento de la medición." },
];

export function AnalyticsPage() {
  const { organization } = useAuth();
  const queryClient = useQueryClient();
  const enqueue = useEnqueue(organization?.id);
  const [metric, setMetric] = useState("alcance");
  const [value, setValue] = useState("");
  const analytics = useQuery({
    queryKey: ["analytics", organization?.id],
    queryFn: ({ signal }) => fetchAnalytics(organization!.id, signal),
    enabled: Boolean(organization),
  });
  const parsedValue = Number(value);
  const canRecord = value !== "" && Number.isFinite(parsedValue) && parsedValue >= 0;
  const record = useMutation({
    mutationFn: () =>
      recordSnapshot(organization!.id, { metric, value: parsedValue, source: "instagram-insights" }),
    onSuccess: () => {
      setValue("");
      void queryClient.invalidateQueries({ queryKey: ["analytics", organization?.id] });
    },
  });

  const snapshots = analytics.data?.snapshots ?? [];
  const latestByMetric = useMemo(() => {
    const map = new Map<string, (typeof snapshots)[number]>();
    for (const item of snapshots) {
      const previous = map.get(item.metric);
      if (!previous || item.capturedAt > previous.capturedAt) {
        map.set(item.metric, item);
      }
    }
    return map;
  }, [snapshots]);

  if (!organization || analytics.isPending) return <Skeleton className="h-64 w-full" />;
  const data = analytics.data;
  const selectedHint = KPI_CHIPS.find((chip) => chip.id === metric)?.hint;
  const empty = !data || (data.snapshots.length === 0 && data.insights.length === 0);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Analítica"
        description="Medimos alcance y seguidores con snapshots reales de Instagram Insights. No hay cifras inventadas."
      />
      <Panel title="Qué medimos">
        <p>
          VapeWave vende por DM o WhatsApp a 15 €, solo colección 60K. Aquí no entra la venta: entra el alcance (que te
          encuentren) y los seguidores (que se queden). Pega el número que ves en Insights; Mavora no lo adivina.
        </p>
        <p className="mt-2">
          Falta cifra objetivo, fecha y presupuesto para el Overview. Hasta entonces, registra snapshots y compara semana
          a semana.{" "}
          <Link className="underline" to="/instagram">
            Ver calendario
          </Link>
        </p>
      </Panel>
      <div className="grid gap-3 sm:grid-cols-2">
        {KPI_CHIPS.map((chip) => {
          const latest = latestByMetric.get(chip.id);
          return (
            <Panel key={chip.id} title={chip.label}>
              {latest ? (
                <>
                  <p className="text-2xl font-semibold text-zinc-900 dark:text-zinc-100">
                    {latest.value.toLocaleString("es-ES")}
                  </p>
                  <p>
                    {new Date(latest.capturedAt).toLocaleString("es-ES", {
                      day: "numeric",
                      month: "short",
                      hour: "2-digit",
                      minute: "2-digit",
                      timeZone: "Europe/Madrid",
                    })}{" "}
                    · {latest.source}
                  </p>
                </>
              ) : (
                <p>Sin snapshot todavía. Ábrelo en Instagram Insights y regístralo abajo.</p>
              )}
            </Panel>
          );
        })}
      </div>
      <form
        className="flex flex-wrap items-end gap-3"
        onSubmit={(event) => {
          event.preventDefault();
          if (canRecord) {
            record.mutate();
          }
        }}
      >
        <div className="space-y-1.5">
          <span className="text-sm font-medium">Métrica</span>
          <div className="flex flex-wrap gap-2">
            {KPI_CHIPS.map((chip) => (
              <Button
                key={chip.id}
                type="button"
                size="sm"
                variant={metric === chip.id ? "primary" : "secondary"}
                onClick={() => setMetric(chip.id)}
              >
                {chip.label}
              </Button>
            ))}
          </div>
          {selectedHint ? <p className="max-w-md text-xs leading-5">{selectedHint}</p> : null}
        </div>
        <label className="space-y-1.5">
          <span className="text-sm font-medium">Valor (el de Insights, no un objetivo)</span>
          <Input
            type="number"
            min={0}
            step={1}
            value={value}
            onChange={(event) => setValue(event.target.value)}
            placeholder="Vacío a propósito"
          />
        </label>
        <Button type="submit" disabled={record.isPending || !canRecord}>
          Registrar snapshot
        </Button>
        <Button
          type="button"
          variant="secondary"
          onClick={() => enqueue.mutate("ANALYTICS_CYCLE")}
          disabled={enqueue.isPending || snapshots.length === 0}
        >
          Generar insights
        </Button>
      </form>
      {empty ? (
        <EmptyState
          title="Sin datos todavía"
          description="Registra alcance y seguidores reales. El ciclo de analítica no corre sin un snapshot: así no inventamos métricas."
        />
      ) : (
        <div className="grid gap-3 lg:grid-cols-2">
          <Panel title="Snapshots">
            <ul className="space-y-1">
              {data.snapshots.map((item) => (
                <li key={item.id}>
                  {item.metric}: {item.value.toLocaleString("es-ES")} ({item.source})
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
