import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { PageHeader } from "@/components/layout/page-header";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { Panel, StatusBadge, parseJsonList } from "@/components/ui/panel";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/features/auth/auth-provider";
import { fetchCampaigns, fetchPublications, publishPublication } from "@/lib/api";
import { useEnqueue } from "@/lib/workspace";

export function CampaignsPage() {
  const { organization } = useAuth();
  const queryClient = useQueryClient();
  const enqueue = useEnqueue(organization?.id);
  const campaigns = useQuery({
    queryKey: ["campaigns", organization?.id],
    queryFn: ({ signal }) => fetchCampaigns(organization!.id, signal),
    enabled: Boolean(organization),
  });
  const publications = useQuery({
    queryKey: ["publications", organization?.id],
    queryFn: ({ signal }) => fetchPublications(organization!.id, signal),
    enabled: Boolean(organization),
  });
  const publish = useMutation({
    mutationFn: (id: string) => publishPublication(organization!.id, id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["publications", organization?.id] });
    },
  });

  if (!organization || campaigns.isPending || publications.isPending) return <Skeleton className="h-64 w-full" />;
  const campaignItems = campaigns.data?.items ?? [];
  const publicationItems = publications.data?.items ?? [];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Campañas y social"
        description="Las campañas nacen de la estrategia aprobada. Publicar es manual: no hay autopilot silencioso."
      />
      {campaignItems.length === 0 ? (
        <EmptyState title="Sin campañas" description="Aprueba una estrategia para crear el primer plan de canales." />
      ) : (
        campaignItems.map((campaign) => (
          <Panel key={campaign.id} title={campaign.name}>
            <StatusBadge value={campaign.status} />
            <p className="mt-2">{parseJsonList(campaign.channelsJson).join(" · ")}</p>
          </Panel>
        ))
      )}
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-sm font-medium">Publicaciones</h2>
        <Button variant="secondary" onClick={() => enqueue.mutate("SOCIAL_PLAN")} disabled={enqueue.isPending}>
          Planificar posts
        </Button>
      </div>
      {publicationItems.length === 0 ? (
        <EmptyState title="Sin publicaciones" description="Aprueba una pieza y lanza el plan social. La publicación real es un clic explícito." />
      ) : (
        publicationItems.map((item) => (
          <Panel key={item.id} title={item.channel}>
            <StatusBadge value={item.status} />
            <p className="mt-2">{item.copy}</p>
            {item.status === "DRAFT" ? (
              <Button className="mt-3" onClick={() => publish.mutate(item.id)} disabled={publish.isPending}>
                Publicar ahora
              </Button>
            ) : (
              <p className="mt-2">Publicado {item.publishedAt ? new Date(item.publishedAt).toLocaleString("es-ES") : ""}</p>
            )}
          </Panel>
        ))
      )}
    </div>
  );
}
