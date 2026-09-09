import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { PageHeader } from "@/components/layout/page-header";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { Input } from "@/components/ui/input";
import { Panel, StatusBadge } from "@/components/ui/panel";
import { Skeleton } from "@/components/ui/skeleton";
import { Textarea } from "@/components/ui/textarea";
import { useAuth } from "@/features/auth/auth-provider";
import {
  ApiError,
  deleteMedia,
  fetchBrandBrief,
  fetchInstagram,
  fetchInstagramPlaybook,
  fetchInstagramSlots,
  fetchMedia,
  generateInstagramWeek,
  uploadMedia,
  upsertBrandBrief,
  type InstagramSlot,
  type MediaAsset,
} from "@/lib/api";
import { useWorkspace } from "@/lib/workspace";

const FORMAT_LABEL: Record<string, string> = {
  REEL: "Reel",
  STORY: "Historia",
  FEED: "Publicación",
  CAROUSEL: "Carrusel",
};

export function InstagramPage() {
  const { organization } = useAuth();
  const queryClient = useQueryClient();
  const [briefError, setBriefError] = useState<string | null>(null);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [captionHint, setCaptionHint] = useState("");

  const workspace = useWorkspace(organization?.id);
  const status = useQuery({
    queryKey: ["instagram", organization?.id],
    queryFn: ({ signal }) => fetchInstagram(organization!.id, signal),
    enabled: Boolean(organization),
  });
  const brief = useQuery({
    queryKey: ["instagram-brief", organization?.id],
    queryFn: ({ signal }) => fetchBrandBrief(organization!.id, signal),
    enabled: Boolean(organization),
  });
  const media = useQuery({
    queryKey: ["instagram-media", organization?.id],
    queryFn: ({ signal }) => fetchMedia(organization!.id, signal),
    enabled: Boolean(organization),
  });
  const slots = useQuery({
    queryKey: ["instagram-slots", organization?.id],
    queryFn: ({ signal }) => fetchInstagramSlots(organization!.id, signal),
    enabled: Boolean(organization),
    refetchInterval: (query) => {
      const items = query.state.data?.items ?? [];
      return items.some((item) => item.status === "SCHEDULED" || item.status === "PUBLISHING") ? 2000 : false;
    },
  });
  const playbook = useQuery({
    queryKey: ["instagram-playbook", organization?.id],
    queryFn: ({ signal }) => fetchInstagramPlaybook(organization!.id, signal),
    enabled: Boolean(organization),
  });

  const saveBrief = useMutation({
    mutationFn: (input: {
      voice: string;
      offer: string;
      cta: string;
      audience: string;
      extraNotes: string;
    }) => upsertBrandBrief(organization!.id, input),
    onSuccess: () => {
      setBriefError(null);
      void queryClient.invalidateQueries({ queryKey: ["instagram-brief", organization?.id] });
    },
    onError: (err) => setBriefError(err instanceof ApiError ? err.message : "No se pudo guardar"),
  });

  const upload = useMutation({
    mutationFn: (file: File) => uploadMedia(organization!.id, file, captionHint || undefined),
    onSuccess: () => {
      setUploadError(null);
      setCaptionHint("");
      void queryClient.invalidateQueries({ queryKey: ["instagram-media", organization?.id] });
    },
    onError: (err) => setUploadError(err instanceof ApiError ? err.message : "No se pudo subir"),
  });

  const remove = useMutation({
    mutationFn: (id: string) => deleteMedia(organization!.id, id),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ["instagram-media", organization?.id] }),
  });

  const generate = useMutation({
    mutationFn: () => generateInstagramWeek(organization!.id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["instagram-slots", organization?.id] });
      void queryClient.invalidateQueries({ queryKey: ["instagram-media", organization?.id] });
      void queryClient.invalidateQueries({ queryKey: ["workspace", organization?.id] });
    },
  });

  if (!organization || status.isPending || brief.isPending || media.isPending || slots.isPending) {
    return <Skeleton className="h-64 w-full" />;
  }

  const connected = Boolean(status.data?.connected);
  const autonomy = Boolean(status.data?.autonomyEnabled);
  const hasCompany = Boolean(workspace.data?.company);
  const items = (slots.data?.items ?? []).filter((slot) => slot.status !== "CANCELLED");
  const assets = media.data?.items ?? [];
  const generateLabel = connected && autonomy ? "Generar semana y publicar" : "Generar semana (copy y horario)";

  return (
    <div className="space-y-6">
      <PageHeader
        title="Calendario de Instagram"
        description="Copy, visual y hora de Madrid. Venta solo por DM o WhatsApp a 15 €. Colección 60K. El alcance y los seguidores se miden en Analítica, no se inventan aquí."
      />

      {!connected ? (
        <Panel title="Sin cuenta de Instagram">
          <p>
            No pasa nada: generamos copy, visuales y el horario (hora de Madrid). Tú lo subes a mano. Cuando quieras
            publicación automática, conecta la cuenta en Integraciones.
          </p>
          <Link className="mt-2 inline-block underline" to="/integrations">
            Ir a Integraciones
          </Link>
        </Panel>
      ) : (
        <Panel title={`@${status.data?.username}`}>
          <div className="flex flex-wrap items-center gap-2">
            <StatusBadge value={status.data?.autonomyEnabled ? "autónomo" : "pausado"} />
            <span>Zona {playbook.data?.timezone ?? "Europe/Madrid"}</span>
            <span>
              {playbook.data?.llmProvider ?? "deepseek"} + {playbook.data?.mediaProvider ?? "fal"}
            </span>
            <Link className="underline" to="/integrations">
              Ajustar conexión
            </Link>
          </div>
          {!status.data?.autonomyEnabled ? (
            <p className="mt-2">La autonomía está apagada: el calendario se genera pero no se publica solo.</p>
          ) : (
            <p className="mt-2">
              Autonomía activa: la primera pieza sale en cuanto el plan está listo; el resto sigue el horario ES.
            </p>
          )}
        </Panel>
      )}

      {!hasCompany && !workspace.isPending ? (
        <Panel title="Falta la empresa">
          <p>Guarda la empresa en Overview (nombre y producto) para que el copy no salga vacío.</p>
          <Link className="mt-2 inline-block underline" to="/">
            Ir a Overview
          </Link>
        </Panel>
      ) : null}

      <Panel title="Brief de marca">
        <p className="mb-3">
          Opcional pero útil. Al conectar Instagram rellenamos estos campos con la bio, la web y los captions. Revisa y
          corrige; no inventamos métricas.
        </p>
        <BriefForm
          initial={brief.data}
          saving={saveBrief.isPending}
          error={briefError}
          onSave={(values) => saveBrief.mutate(values)}
        />
      </Panel>

      <Panel title="Logo y fotos de producto">
        <p className="mb-3">
          Sube el logo y fotos reales de la 60K (JPEG/PNG) o un reel (MP4). Si hay fotos vuestras, el calendario las usa.
          Si no, generamos. Pista útil: sabor, logo o packshot.
        </p>
        <div className="mb-4 flex flex-col gap-2 sm:flex-row">
          <Input
            value={captionHint}
            onChange={(event) => setCaptionHint(event.target.value)}
            placeholder="Pista para el copy (producto, beneficio…)"
          />
          <Input
            type="file"
            accept="image/jpeg,image/png,video/mp4"
            onChange={(event) => {
              const file = event.target.files?.[0];
              if (file) {
                upload.mutate(file);
              }
              event.target.value = "";
            }}
          />
        </div>
        {uploadError ? <p className="mb-2 text-red-700 dark:text-red-400">{uploadError}</p> : null}
        {assets.length === 0 ? (
          <p>Sin fotos propias: usaremos visuales generados.</p>
        ) : (
          <ul className="grid gap-3 sm:grid-cols-2">
            {assets.map((asset) => (
              <li key={asset.id} className="rounded-md border border-zinc-200 p-3 dark:border-zinc-800">
                {asset.kind === "IMAGE" ? (
                  <img src={asset.url} alt={asset.filename} className="mb-2 h-32 w-full rounded object-cover" />
                ) : (
                  <p className="mb-2 text-xs uppercase">Vídeo</p>
                )}
                <p className="truncate font-medium text-zinc-900 dark:text-zinc-100">{asset.filename}</p>
                <p>{asset.generated ? "Generada" : "Vuestra"}</p>
                {asset.captionHint && !asset.generated ? <p>{asset.captionHint}</p> : null}
                <Button
                  className="mt-2"
                  variant="ghost"
                  size="sm"
                  onClick={() => remove.mutate(asset.id)}
                  disabled={remove.isPending}
                >
                  Quitar
                </Button>
              </li>
            ))}
          </ul>
        )}
      </Panel>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-sm font-medium">Calendario de la semana</h2>
        <Button onClick={() => generate.mutate()} disabled={generate.isPending}>
          {generate.isPending ? "Planificando…" : generateLabel}
        </Button>
      </div>
      {items.length > 0 ? (
        <p>
          Pedido por DM o WhatsApp. 15 € la unidad. Solo 60K.{" "}
          <Link className="underline" to="/analytics">
            Registrar alcance y seguidores
          </Link>
          .
        </p>
      ) : null}
      {generate.error ? (
        <p className="text-red-700 dark:text-red-400">
          {generate.error instanceof ApiError ? generate.error.message : "No se pudo generar la semana"}
        </p>
      ) : null}

      {items.length === 0 ? (
        <EmptyState
          title="Sin piezas programadas"
          description="Genera la semana: copy, visual y la hora de Madrid en la que conviene subirlo. Conectar Instagram es opcional."
        />
      ) : (
        items.map((slot) => (
          <SlotCard key={slot.id} slot={slot} assets={assets} connected={connected} autonomy={autonomy} />
        ))
      )}

      {playbook.data ? (
        <Panel title="Playbook del algoritmo">
          <pre className="whitespace-pre-wrap font-sans text-sm leading-6">{playbook.data.principles}</pre>
        </Panel>
      ) : null}
    </div>
  );
}

function SlotCard({
  slot,
  assets,
  connected,
  autonomy,
}: {
  slot: InstagramSlot;
  assets: MediaAsset[];
  connected: boolean;
  autonomy: boolean;
}) {
  const visuals = slot.mediaAssetIds
    .map((id) => assets.find((asset) => asset.id === id))
    .filter((asset): asset is MediaAsset => Boolean(asset));
  const fullCopy = [slot.hook, slot.caption, slot.cta, slot.hashtags.join(" ")].filter(Boolean).join("\n\n");
  const statusLabel =
    slot.status === "SCHEDULED"
      ? connected && autonomy
        ? "programado"
        : "súbelo a mano"
      : slot.status.toLowerCase();

  return (
    <Panel title={`${FORMAT_LABEL[slot.format] ?? slot.format} · ${formatWhen(slot.scheduledAt)}`}>
      <div className="flex flex-wrap items-center gap-2">
        <StatusBadge value={statusLabel} />
      </div>
      <div className="mt-3 rounded-md border border-zinc-200 bg-zinc-50 px-3 py-2 dark:border-zinc-800 dark:bg-zinc-900">
        <p className="text-xs font-medium uppercase tracking-wide text-zinc-500">Cuándo subirlo</p>
        <p className="text-base font-medium text-zinc-900 dark:text-zinc-100">{formatWhen(slot.scheduledAt)}</p>
        <p className="text-xs">Hora de Madrid. {connected && autonomy ? "Mavora lo publica sola." : "Cópialo y súbelo tú en Instagram."}</p>
      </div>
      {visuals.length > 0 ? (
        <ul className="mt-3 grid gap-2 sm:grid-cols-2">
          {visuals.map((asset) => (
            <li key={asset.id}>
              {asset.kind === "IMAGE" ? (
                <img src={asset.url} alt={asset.filename} className="h-40 w-full rounded object-cover" />
              ) : (
                <p className="text-xs uppercase">Vídeo · {asset.filename}</p>
              )}
            </li>
          ))}
        </ul>
      ) : null}
      <p className="mt-3 font-medium text-zinc-900 dark:text-zinc-100">{slot.hook}</p>
      <p className="mt-1 whitespace-pre-wrap">{slot.caption}</p>
      <p className="mt-1">{slot.cta}</p>
      {slot.hashtags.length > 0 ? <p className="mt-1">{slot.hashtags.join(" ")}</p> : null}
      <div className="mt-3 flex flex-wrap gap-2">
        <CopyButton label="Copiar hook" value={slot.hook} />
        <CopyButton label="Copiar copy" value={slot.caption} />
        <CopyButton label="Copiar CTA" value={slot.cta} />
        {slot.hashtags.length > 0 ? <CopyButton label="Copiar hashtags" value={slot.hashtags.join(" ")} /> : null}
        <CopyButton label="Copiar pieza" value={fullCopy} />
      </div>
      {slot.igMediaId ? <p className="mt-1 text-xs">id {slot.igMediaId}</p> : null}
      {slot.errorMessage ? <p className="mt-1 text-red-700 dark:text-red-400">{slot.errorMessage}</p> : null}
    </Panel>
  );
}

function CopyButton({ label, value }: { label: string; value: string }) {
  const [copied, setCopied] = useState(false);

  async function copy() {
    setCopied(true);
    window.setTimeout(() => setCopied(false), 2000);
    try {
      await navigator.clipboard.writeText(value);
      return;
    } catch {
      /* sandbox browsers often block the clipboard API */
    }
    const textarea = document.createElement("textarea");
    textarea.value = value;
    textarea.setAttribute("readonly", "");
    textarea.style.position = "fixed";
    textarea.style.left = "-9999px";
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand("copy");
    document.body.removeChild(textarea);
  }

  return (
    <Button type="button" variant="secondary" size="sm" onClick={() => void copy()}>
      {copied ? "Copiado" : label}
    </Button>
  );
}

function formatWhen(iso: string): string {
  return new Date(iso).toLocaleString("es-ES", {
    weekday: "long",
    day: "numeric",
    month: "long",
    hour: "2-digit",
    minute: "2-digit",
    timeZone: "Europe/Madrid",
  });
}

function BriefForm({
  initial,
  saving,
  error,
  onSave,
}: {
  initial:
    | {
        voice: string | null;
        offer: string | null;
        cta: string | null;
        audience: string | null;
        extraNotes: string | null;
      }
    | undefined;
  saving: boolean;
  error: string | null;
  onSave: (values: {
    voice: string;
    offer: string;
    cta: string;
    audience: string;
    extraNotes: string;
  }) => void;
}) {
  const [voice, setVoice] = useState(initial?.voice ?? "");
  const [offer, setOffer] = useState(initial?.offer ?? "");
  const [cta, setCta] = useState(initial?.cta ?? "");
  const [audience, setAudience] = useState(initial?.audience ?? "");
  const [extraNotes, setExtraNotes] = useState(initial?.extraNotes ?? "");

  useEffect(() => {
    setVoice(initial?.voice ?? "");
    setOffer(initial?.offer ?? "");
    setCta(initial?.cta ?? "");
    setAudience(initial?.audience ?? "");
    setExtraNotes(initial?.extraNotes ?? "");
  }, [initial?.voice, initial?.offer, initial?.cta, initial?.audience, initial?.extraNotes]);

  return (
    <form
      className="space-y-3"
      onSubmit={(event) => {
        event.preventDefault();
        onSave({ voice, offer, cta, audience, extraNotes });
      }}
    >
      <Input value={voice} onChange={(event) => setVoice(event.target.value)} placeholder="Voz de marca" />
      <Textarea value={offer} onChange={(event) => setOffer(event.target.value)} placeholder="Oferta y producto" />
      <Input
        value={cta}
        onChange={(event) => setCta(event.target.value)}
        placeholder="CTA de venta (DM o WhatsApp. 15 €. Solo +18.)"
      />
      <Input value={audience} onChange={(event) => setAudience(event.target.value)} placeholder="Audiencia / ICP" />
      <Textarea
        value={extraNotes}
        onChange={(event) => setExtraNotes(event.target.value)}
        placeholder="Notas, fotos que no puedes subir aún, objeciones, prueba social…"
      />
      <Button type="submit" disabled={saving}>
        {saving ? "Guardando…" : "Guardar brief"}
      </Button>
      {error ? <p className="text-red-700 dark:text-red-400">{error}</p> : null}
    </form>
  );
}
