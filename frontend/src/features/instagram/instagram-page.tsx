import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
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
} from "@/lib/api";

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

  const connected = status.data?.connected;
  const items = (slots.data?.items ?? []).filter((slot) => slot.status !== "CANCELLED");
  const assets = media.data?.items ?? [];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Instagram autónomo"
        description="DeepSeek escribe el copy. Fal.ai genera las imágenes y los reels. Mavora planifica y publica sin bandeja de aprobación."
      />

      {!connected ? (
        <EmptyState
          title="Conecta Instagram"
          description="Empieza en Integraciones. Solo cuentas profesionales. El token no se guarda en texto plano."
        />
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

      <Panel title="Brief de marca">
        <p className="mb-3">Empresa, oferta, voz y CTA. Mavora ya usa también el perfil y los productos del overview.</p>
        <BriefForm
          initial={brief.data}
          saving={saveBrief.isPending}
          error={briefError}
          onSave={(values) => saveBrief.mutate(values)}
        />
      </Panel>

      <Panel title="Fotos y vídeos">
        <p className="mb-3">
          Opcional: fotos de producto como referencia. Fal.ai genera la pieza de cada slot (JPEG 4:5 o 9:16; MP4 para
          reels).
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
          <p>Sube al menos una foto para poder generar la semana.</p>
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
                {asset.captionHint ? <p>{asset.captionHint}</p> : null}
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
        <Button
          onClick={() => generate.mutate()}
          disabled={!connected || generate.isPending}
        >
          {generate.isPending ? "Planificando…" : "Generar semana y publicar"}
        </Button>
      </div>
      {generate.error ? (
        <p className="text-red-700 dark:text-red-400">
          {generate.error instanceof ApiError ? generate.error.message : "No se pudo generar la semana"}
        </p>
      ) : null}

      {items.length === 0 ? (
        <EmptyState
          title="Sin piezas programadas"
          description="Conecta la cuenta y genera la semana. DeepSeek + Fal.ai crean copy y visuales; las fotos de marca solo afinan el contexto."
        />
      ) : (
        items.map((slot) => (
          <Panel key={slot.id} title={`${FORMAT_LABEL[slot.format] ?? slot.format} · ${formatWhen(slot.scheduledAt)}`}>
            <StatusBadge value={slot.status} />
            <p className="mt-2 font-medium text-zinc-900 dark:text-zinc-100">{slot.hook}</p>
            <p className="mt-1">{slot.caption}</p>
            <p className="mt-1">{slot.cta}</p>
            {slot.hashtags.length > 0 ? <p className="mt-1">{slot.hashtags.join(" ")}</p> : null}
            {slot.igMediaId ? <p className="mt-1 text-xs">id {slot.igMediaId}</p> : null}
            {slot.errorMessage ? <p className="mt-1 text-red-700 dark:text-red-400">{slot.errorMessage}</p> : null}
          </Panel>
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

function formatWhen(iso: string): string {
  return new Date(iso).toLocaleString("es-ES", { dateStyle: "medium", timeStyle: "short" });
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
      <Input value={cta} onChange={(event) => setCta(event.target.value)} placeholder="CTA de venta (bio, DM, demo…)" />
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
