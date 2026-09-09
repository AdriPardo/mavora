import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { PageHeader } from "@/components/layout/page-header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Panel, StatusBadge } from "@/components/ui/panel";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-provider";
import {
  ApiError,
  connectInstagramFake,
  disconnectInstagram,
  fetchInstagram,
  fetchInstagramConnectUrl,
  setInstagramAutonomy,
} from "@/lib/api";

export function IntegrationsPage() {
  const { organization } = useAuth();
  const queryClient = useQueryClient();
  const [params] = useSearchParams();
  const [username, setUsername] = useState("acme.demo");
  const [error, setError] = useState<string | null>(null);
  const oauthResult = params.get("instagram");

  const status = useQuery({
    queryKey: ["instagram", organization?.id],
    queryFn: ({ signal }) => fetchInstagram(organization!.id, signal),
    enabled: Boolean(organization),
  });

  useEffect(() => {
    if (oauthResult === "connected") {
      void queryClient.invalidateQueries({ queryKey: ["instagram", organization?.id] });
    }
  }, [oauthResult, organization?.id, queryClient]);

  const connectFake = useMutation({
    mutationFn: () => connectInstagramFake(organization!.id, username),
    onSuccess: () => {
      setError(null);
      void queryClient.invalidateQueries({ queryKey: ["instagram", organization?.id] });
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : "No se pudo conectar"),
  });

  const connectMeta = useMutation({
    mutationFn: () => fetchInstagramConnectUrl(organization!.id),
    onSuccess: (data) => {
      window.location.assign(data.url);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : "OAuth no está configurado"),
  });

  const autonomy = useMutation({
    mutationFn: (enabled: boolean) => setInstagramAutonomy(organization!.id, enabled),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["instagram", organization?.id] }),
  });

  const disconnect = useMutation({
    mutationFn: () => disconnectInstagram(organization!.id),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["instagram", organization?.id] }),
  });

  if (!organization || status.isPending) {
    return <Skeleton className="h-64 w-full" />;
  }

  const data = status.data;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Integraciones"
        description="Conecta canales. Los tokens se cifran; nunca viajan de vuelta a la UI. Instagram Professional (Business o Creator) es el único modo soportado."
      />
      {oauthResult === "connected" ? (
        <p className="text-sm text-emerald-700 dark:text-emerald-400">Instagram conectado.</p>
      ) : null}
      {oauthResult === "error" ? (
        <p className="text-sm text-red-700 dark:text-red-400">No se pudo completar el OAuth de Instagram.</p>
      ) : null}

      <Panel title="Instagram">
        <p className="mb-3">
          Mavora gestiona reels, historias, publicaciones y carruseles al 100% si dejas la autonomía encendida. Hace
          falta una cuenta profesional vinculada a una Página de Facebook.
        </p>
        {data?.connected ? (
          <div className="space-y-3">
            <div className="flex flex-wrap items-center gap-2">
              <StatusBadge value="conectado" />
              <span className="font-medium text-zinc-900 dark:text-zinc-100">@{data.username}</span>
              <span className="text-xs uppercase tracking-wide">{data.provider}</span>
            </div>
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={data.autonomyEnabled}
                onChange={(event) => autonomy.mutate(event.target.checked)}
              />
              Autonomía total (sin aprobación humana)
            </label>
            <div className="flex flex-wrap items-center gap-3">
              <Link className="text-zinc-900 underline dark:text-zinc-100" to="/instagram">
                Ir al calendario
              </Link>
              <Button variant="secondary" onClick={() => disconnect.mutate()} disabled={disconnect.isPending}>
                Desconectar
              </Button>
            </div>
          </div>
        ) : (
          <div className="space-y-3">
            {data?.provider === "fake" ? (
              <>
                <p>Modo local: conecta una cuenta de demostración. En producción configura Meta (INSTAGRAM_PROVIDER=meta).</p>
                <div className="flex max-w-md flex-col gap-2 sm:flex-row">
                  <Input
                    value={username}
                    onChange={(event) => setUsername(event.target.value)}
                    placeholder="usuario"
                    aria-label="Usuario de Instagram"
                  />
                  <Button onClick={() => connectFake.mutate()} disabled={connectFake.isPending}>
                    {connectFake.isPending ? "Conectando…" : "Conectar demo"}
                  </Button>
                </div>
              </>
            ) : (
              <Button onClick={() => connectMeta.mutate()} disabled={connectMeta.isPending}>
                Conectar Instagram profesional
              </Button>
            )}
            {data?.provider === "fake" ? (
              <Button variant="ghost" onClick={() => connectMeta.mutate()} disabled={connectMeta.isPending}>
                Probar URL de Meta
              </Button>
            ) : null}
          </div>
        )}
        {error ? <p className="mt-3 text-red-700 dark:text-red-400">{error}</p> : null}
      </Panel>
    </div>
  );
}
