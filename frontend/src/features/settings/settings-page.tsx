import { useQuery } from "@tanstack/react-query";
import { PageHeader } from "@/components/layout/page-header";
import { ThemeToggle } from "@/components/theme/theme-toggle";
import { Skeleton } from "@/components/ui/skeleton";
import { fetchHealth } from "@/lib/api";

export function SettingsPage() {
  const health = useQuery({
    queryKey: ["health"],
    queryFn: ({ signal }) => fetchHealth(signal),
    retry: 1,
  });

  return (
    <div className="space-y-8">
      <PageHeader
        title="Ajustes"
        description="Preferencias de la interfaz y estado del sistema."
      />

      <section className="rounded-lg border border-zinc-200 bg-white p-5 dark:border-zinc-800 dark:bg-zinc-950">
        <h2 className="text-sm font-medium">Apariencia</h2>
        <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
          Claro, oscuro o según el sistema.
        </p>
        <div className="mt-4">
          <ThemeToggle />
        </div>
      </section>

      <section className="rounded-lg border border-zinc-200 bg-white p-5 dark:border-zinc-800 dark:bg-zinc-950">
        <h2 className="text-sm font-medium">API</h2>
        <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
          Comprobación de disponibilidad del backend.
        </p>
        <div className="mt-4 text-sm">
          {health.isPending ? (
            <Skeleton className="h-5 w-48" />
          ) : health.isError ? (
            <p className="text-red-700 dark:text-red-400">
              No se pudo conectar con la API. Arranca el backend en el puerto 8080.
            </p>
          ) : (
            <p className="text-zinc-700 dark:text-zinc-300">
              Estado: <span className="font-medium">{health.data.status}</span>
              <span className="mx-2 text-zinc-300 dark:text-zinc-700">·</span>
              Servicio: <span className="font-medium">{health.data.service}</span>
            </p>
          )}
        </div>
      </section>
    </div>
  );
}
