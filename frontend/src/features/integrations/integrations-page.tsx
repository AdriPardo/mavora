import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type ReactNode, useEffect, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { PageHeader } from "@/components/layout/page-header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Panel, StatusBadge } from "@/components/ui/panel";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-provider";
import {
  ApiError,
  clearMetaSetup,
  connectInstagramFake,
  connectInstagramToken,
  disconnectInstagram,
  fetchInstagram,
  fetchInstagramConnectUrl,
  fetchMetaSetup,
  saveMetaSetup,
  setInstagramAutonomy,
  type InstagramStatus,
} from "@/lib/api";

export function IntegrationsPage() {
  const { organization } = useAuth();
  const queryClient = useQueryClient();
  const [params] = useSearchParams();
  const [error, setError] = useState<string | null>(null);
  const oauthResult = params.get("instagram");

  const status = useQuery({
    queryKey: ["instagram", organization?.id],
    queryFn: ({ signal }) => fetchInstagram(organization!.id, signal),
    enabled: Boolean(organization),
  });
  const meta = useQuery({
    queryKey: ["instagram-meta", organization?.id],
    queryFn: ({ signal }) => fetchMetaSetup(organization!.id, signal),
    enabled: Boolean(organization),
  });

  useEffect(() => {
    if (oauthResult === "connected") {
      void queryClient.invalidateQueries({ queryKey: ["instagram", organization?.id] });
      void queryClient.invalidateQueries({ queryKey: ["instagram-brief", organization?.id] });
      void queryClient.invalidateQueries({ queryKey: ["workspace", organization?.id] });
    }
  }, [oauthResult, organization?.id, queryClient]);

  if (!organization || status.isPending || meta.isPending) {
    return <Skeleton className="h-64 w-full" />;
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Integraciones"
        description="Conecta Instagram profesional con tu app de Meta. Las claves y tokens se cifran; el App Secret y los tokens no vuelven a la UI."
      />
      {oauthResult === "connected" || status.data?.connected ? (
        <ImportBanner status={status.data} oauthJustFinished={oauthResult === "connected"} />
      ) : null}
      {oauthResult === "error" ? (
        <p className="text-sm text-red-700 dark:text-red-400">No se pudo completar el OAuth de Instagram.</p>
      ) : null}

      <MetaSetupPanel
        organizationId={organization.id}
        onSaved={() => {
          setError(null);
          void queryClient.invalidateQueries({ queryKey: ["instagram-meta", organization.id] });
          void queryClient.invalidateQueries({ queryKey: ["instagram", organization.id] });
        }}
        onError={setError}
      />

      <InstagramAccountPanel
        organizationId={organization.id}
        oauthReady={Boolean(meta.data?.oauthReady || status.data?.oauthReady)}
        onError={setError}
      />

      <DemoPanel
        organizationId={organization.id}
        connected={Boolean(status.data?.connected)}
        onError={setError}
      />

      {error ? <p className="text-sm text-red-700 dark:text-red-400">{error}</p> : null}
    </div>
  );
}

function MetaSetupPanel({
  organizationId,
  onSaved,
  onError,
}: {
  organizationId: string;
  onSaved: () => void;
  onError: (message: string | null) => void;
}) {
  const meta = useQuery({
    queryKey: ["instagram-meta", organizationId],
    queryFn: ({ signal }) => fetchMetaSetup(organizationId, signal),
  });
  const [appId, setAppId] = useState("");
  const [appSecret, setAppSecret] = useState("");
  const [graphVersion, setGraphVersion] = useState("v21.0");
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    if (!meta.data || hydrated) {
      return;
    }
    setAppId(meta.data.appId);
    setGraphVersion(meta.data.graphVersion);
    setHydrated(true);
  }, [meta.data, hydrated]);

  const save = useMutation({
    mutationFn: () =>
      saveMetaSetup(organizationId, {
        appId,
        appSecret: appSecret.trim() ? appSecret : undefined,
        graphVersion,
      }),
    onSuccess: () => {
      setAppSecret("");
      setHydrated(false);
      onSaved();
    },
    onError: (err) => onError(err instanceof ApiError ? err.message : "No se pudieron guardar las claves de Meta"),
  });

  const clear = useMutation({
    mutationFn: () => clearMetaSetup(organizationId),
    onSuccess: () => {
      setAppId("");
      setAppSecret("");
      setHydrated(false);
      onSaved();
    },
    onError: (err) => onError(err instanceof ApiError ? err.message : "No se pudieron borrar las claves"),
  });

  const data = meta.data;
  if (!data) {
    return <Skeleton className="h-64 w-full" />;
  }

  return (
    <Panel title="1. App de Meta (claves)">
      <p className="mb-4">
        Necesitas una app en el{" "}
        <a
          className="text-zinc-900 underline dark:text-zinc-100"
          href={data.developerConsoleUrl}
          target="_blank"
          rel="noreferrer"
        >
          panel de desarrolladores de Meta
        </a>
        . Tipo Empresa. Añade Facebook Login e Instagram.
      </p>

      <ol className="mb-5 list-decimal space-y-2 pl-5">
        <li>
          Crea la app y copia el <strong className="font-medium text-zinc-900 dark:text-zinc-100">Identificador</strong>{" "}
          (App ID) y la <strong className="font-medium text-zinc-900 dark:text-zinc-100">clave secreta</strong> (App
          Secret).
        </li>
        <li>
          En Facebook Login → Ajustes, pega esta URI de redirección OAuth (tiene que coincidir al carácter):
          <CopyField value={data.suggestedRedirectUri} label="URI de redirección OAuth" />
        </li>
        <li>
          En Ajustes básicos, pon como dominio y URL del sitio la API pública:{" "}
          <code className="rounded bg-zinc-100 px-1 text-xs dark:bg-zinc-900">{data.publicApiUrl}</code>
        </li>
        <li>
          Pide estos permisos (modo desarrollo: añade tu usuario de Facebook como tester o desarrollador):
          <ul className="mt-1 flex flex-wrap gap-1">
            {data.scopes.map((scope) => (
              <li key={scope}>
                <code className="rounded bg-zinc-100 px-1.5 py-0.5 text-xs dark:bg-zinc-900">{scope}</code>
              </li>
            ))}
          </ul>
        </li>
        <li>
          Instagram tiene que ser <strong className="font-medium text-zinc-900 dark:text-zinc-100">Professional</strong>{" "}
          (Business o Creator) y estar ligado a una Página de Facebook.
        </li>
        <li>
          Meta descarga las fotos y vídeos desde{" "}
          <code className="rounded bg-zinc-100 px-1 text-xs dark:bg-zinc-900">{data.publicApiUrl}</code>. En local no
          podrá publicar de verdad.
        </li>
      </ol>

      <form
        className="grid max-w-xl gap-3"
        onSubmit={(event) => {
          event.preventDefault();
          onError(null);
          save.mutate();
        }}
      >
        <Field label="App ID">
          <Input
            value={appId}
            onChange={(event) => setAppId(event.target.value)}
            placeholder="123456789012345"
            autoComplete="off"
            inputMode="numeric"
            aria-label="App ID de Meta"
          />
        </Field>
        <Field
          label="App Secret"
          hint={
            data.secretConfigured
              ? "Ya hay un secreto guardado. Déjalo vacío para no cambiarlo."
              : "Se cifra al guardar. No lo mostramos otra vez."
          }
        >
          <Input
            type="password"
            value={appSecret}
            onChange={(event) => setAppSecret(event.target.value)}
            placeholder={data.secretConfigured ? "••••••••••••" : "Pega el App Secret"}
            autoComplete="new-password"
            aria-label="App Secret de Meta"
          />
        </Field>
        <Field label="Versión de Graph API" hint="La que use tu app. Por defecto v21.0.">
          <Input
            value={graphVersion}
            onChange={(event) => setGraphVersion(event.target.value)}
            placeholder="v21.0"
            aria-label="Versión de Graph API"
          />
        </Field>
        <div className="flex flex-wrap items-center gap-2 pt-1">
          <Button type="submit" disabled={save.isPending || !appId.trim()}>
            {save.isPending ? "Guardando…" : data.secretConfigured ? "Actualizar claves" : "Guardar claves"}
          </Button>
          {data.secretConfigured || data.appId ? (
            <Button
              type="button"
              variant="ghost"
              onClick={() => {
                onError(null);
                clear.mutate();
              }}
              disabled={clear.isPending}
            >
              Borrar claves
            </Button>
          ) : null}
          <StatusBadge
            value={
              data.oauthReady
                ? data.source === "environment"
                  ? "listo (entorno)"
                  : "listo"
                : "faltan claves"
            }
          />
        </div>
      </form>
    </Panel>
  );
}

function InstagramAccountPanel({
  organizationId,
  oauthReady,
  onError,
}: {
  organizationId: string;
  oauthReady: boolean;
  onError: (message: string | null) => void;
}) {
  const queryClient = useQueryClient();
  const status = useQuery({
    queryKey: ["instagram", organizationId],
    queryFn: ({ signal }) => fetchInstagram(organizationId, signal),
  });
  const [showToken, setShowToken] = useState(false);
  const [username, setUsername] = useState("");
  const [igUserId, setIgUserId] = useState("");
  const [pageId, setPageId] = useState("");
  const [accessToken, setAccessToken] = useState("");

  const connectMeta = useMutation({
    mutationFn: () => fetchInstagramConnectUrl(organizationId),
    onSuccess: (data) => {
      window.location.assign(data.url);
    },
    onError: (err) => onError(err instanceof ApiError ? err.message : "OAuth no está configurado"),
  });

  const connectToken = useMutation({
    mutationFn: () =>
      connectInstagramToken(organizationId, {
        username,
        igUserId,
        pageId: pageId.trim() || undefined,
        accessToken,
      }),
    onSuccess: () => {
      setAccessToken("");
      onError(null);
      void queryClient.invalidateQueries({ queryKey: ["instagram", organizationId] });
      void queryClient.invalidateQueries({ queryKey: ["instagram-brief", organizationId] });
      void queryClient.invalidateQueries({ queryKey: ["workspace", organizationId] });
    },
    onError: (err) => onError(err instanceof ApiError ? err.message : "No se pudo guardar el token"),
  });

  const autonomy = useMutation({
    mutationFn: (enabled: boolean) => setInstagramAutonomy(organizationId, enabled),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["instagram", organizationId] }),
  });

  const disconnect = useMutation({
    mutationFn: () => disconnectInstagram(organizationId),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["instagram", organizationId] }),
  });

  const data = status.data;

  return (
    <Panel title="2. Conectar Instagram profesional">
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
        <div className="space-y-4">
          <p>
            Con las claves guardadas, Meta te pide permiso. Tiene que ser una cuenta Professional ligada a una Página.
          </p>
          <Button
            onClick={() => {
              onError(null);
              connectMeta.mutate();
            }}
            disabled={connectMeta.isPending || !oauthReady}
          >
            {connectMeta.isPending ? "Abriendo Meta…" : "Conectar con Facebook / Instagram"}
          </Button>
          {!oauthReady ? (
            <p className="text-amber-800 dark:text-amber-300">
              Primero guarda el App ID y el App Secret en el paso 1.
            </p>
          ) : null}

          <div className="border-t border-zinc-200 pt-4 dark:border-zinc-800">
            <button
              type="button"
              className="text-sm text-zinc-900 underline dark:text-zinc-100"
              onClick={() => setShowToken((value) => !value)}
            >
              {showToken ? "Ocultar conexión con token" : "Ya tengo un token de página (Graph API Explorer)"}
            </button>
            {showToken ? (
              <form
                className="mt-3 grid max-w-xl gap-3"
                onSubmit={(event) => {
                  event.preventDefault();
                  onError(null);
                  connectToken.mutate();
                }}
              >
                <p>
                  En Graph API Explorer genera un token de página con los mismos permisos. No pegues un token de usuario
                  corto.
                </p>
                <Field label="Usuario de Instagram">
                  <Input
                    value={username}
                    onChange={(event) => setUsername(event.target.value)}
                    placeholder="tu.marca"
                    aria-label="Usuario de Instagram"
                  />
                </Field>
                <Field label="Instagram Business Account ID">
                  <Input
                    value={igUserId}
                    onChange={(event) => setIgUserId(event.target.value)}
                    placeholder="1784…"
                    aria-label="Instagram Business Account ID"
                  />
                </Field>
                <Field label="Page ID de Facebook" hint="Opcional, pero recomendable.">
                  <Input
                    value={pageId}
                    onChange={(event) => setPageId(event.target.value)}
                    placeholder="1234567890"
                    aria-label="Page ID de Facebook"
                  />
                </Field>
                <Field label="Token de página">
                  <Input
                    type="password"
                    value={accessToken}
                    onChange={(event) => setAccessToken(event.target.value)}
                    placeholder="EAAG…"
                    autoComplete="off"
                    aria-label="Token de página de Meta"
                  />
                </Field>
                <Button type="submit" disabled={connectToken.isPending}>
                  {connectToken.isPending ? "Guardando…" : "Guardar token y conectar"}
                </Button>
              </form>
            ) : null}
          </div>
        </div>
      )}
    </Panel>
  );
}

function DemoPanel({
  organizationId,
  connected,
  onError,
}: {
  organizationId: string;
  connected: boolean;
  onError: (message: string | null) => void;
}) {
  const queryClient = useQueryClient();
  const [username, setUsername] = useState("acme.demo");
  const [open, setOpen] = useState(false);
  const connectFake = useMutation({
    mutationFn: () => connectInstagramFake(organizationId, username),
    onSuccess: () => {
      onError(null);
      void queryClient.invalidateQueries({ queryKey: ["instagram", organizationId] });
      void queryClient.invalidateQueries({ queryKey: ["instagram-brief", organizationId] });
      void queryClient.invalidateQueries({ queryKey: ["workspace", organizationId] });
    },
    onError: (err) => onError(err instanceof ApiError ? err.message : "No se pudo conectar la demo"),
  });

  if (connected) {
    return null;
  }

  return (
    <Panel title="Modo prueba (sin Meta)">
      <button
        type="button"
        className="text-sm text-zinc-900 underline dark:text-zinc-100"
        onClick={() => setOpen((value) => !value)}
      >
        {open ? "Ocultar demo" : "Conectar una cuenta de demostración"}
      </button>
      {open ? (
        <div className="mt-3 space-y-3">
          <p>Solo para probar el calendario en local. No publica en Instagram de verdad.</p>
          <div className="flex max-w-md flex-col gap-2 sm:flex-row">
            <Input
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              placeholder="usuario"
              aria-label="Usuario de Instagram de demostración"
            />
            <Button
              onClick={() => {
                onError(null);
                connectFake.mutate();
              }}
              disabled={connectFake.isPending}
            >
              {connectFake.isPending ? "Conectando…" : "Conectar demo"}
            </Button>
          </div>
        </div>
      ) : null}
    </Panel>
  );
}

function ImportBanner({
  status,
  oauthJustFinished,
}: {
  status?: InstagramStatus;
  oauthJustFinished: boolean;
}) {
  if (!status?.connected) {
    return oauthJustFinished ? (
      <p className="text-sm text-emerald-700 dark:text-emerald-400">Instagram conectado.</p>
    ) : null;
  }
  const fields = status.filledFromProfile ?? [];
  return (
    <Panel title="Perfil analizado">
      <p className="mb-2">
        {status.profileSummary ?? `Hemos leído @${status.username} y rellenado lo que el perfil deja claro.`}
      </p>
      {fields.length > 0 ? (
        <p>
          Campos: {fields.join(", ")}. Revisa Overview y el brief de Instagram; no inventamos métricas ni presupuesto.
        </p>
      ) : (
        <p>No había huecos que rellenar: la empresa o el brief ya tenían datos.</p>
      )}
      <div className="mt-2 flex flex-wrap gap-3">
        <Link className="text-zinc-900 underline dark:text-zinc-100" to="/overview">
          Ver empresa
        </Link>
        <Link className="text-zinc-900 underline dark:text-zinc-100" to="/instagram">
          Ver brief
        </Link>
      </div>
    </Panel>
  );
}

function Field({
  label,
  hint,
  children,
}: {
  label: string;
  hint?: string;
  children: ReactNode;
}) {
  return (
    <label className="block">
      <span className="mb-1 block text-xs font-medium uppercase tracking-wide text-zinc-500">{label}</span>
      {children}
      {hint ? <span className="mt-1 block text-xs text-zinc-500">{hint}</span> : null}
    </label>
  );
}

function CopyField({ value, label }: { value: string; label: string }) {
  const [copied, setCopied] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  async function copy() {
    setCopied(true);
    window.setTimeout(() => setCopied(false), 2000);
    try {
      await navigator.clipboard.writeText(value);
      return;
    } catch {
      /* sandbox browsers often block the clipboard API */
    }
    const input = inputRef.current;
    if (!input) {
      return;
    }
    input.focus();
    input.select();
    document.execCommand("copy");
  }

  return (
    <div className="mt-2 flex flex-col gap-2 sm:flex-row sm:items-center">
      <Input ref={inputRef} readOnly value={value} aria-label={label} className="font-mono text-xs" />
      <Button type="button" variant="secondary" onClick={() => void copy()}>
        {copied ? "Copiado" : "Copiar"}
      </Button>
    </div>
  );
}
