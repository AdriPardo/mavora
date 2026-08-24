import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { enqueueWorkflow, fetchWorkspace, type Workspace } from "@/lib/api";

export function useWorkspace(organizationId: string | undefined) {
  return useQuery({
    queryKey: ["workspace", organizationId],
    queryFn: ({ signal }) => fetchWorkspace(organizationId!, signal),
    enabled: Boolean(organizationId),
    refetchInterval: (query) => {
      const data = query.state.data as Workspace | undefined;
      const busy = data?.workflows.some((item) => item.status === "QUEUED" || item.status === "RUNNING");
      return busy ? 1500 : false;
    },
  });
}

export function useEnqueue(organizationId: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (type: string) => enqueueWorkflow(organizationId!, type),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["workspace", organizationId] });
      void queryClient.invalidateQueries({ queryKey: ["research", organizationId] });
      void queryClient.invalidateQueries({ queryKey: ["content", organizationId] });
      void queryClient.invalidateQueries({ queryKey: ["campaigns", organizationId] });
      void queryClient.invalidateQueries({ queryKey: ["publications", organizationId] });
      void queryClient.invalidateQueries({ queryKey: ["analytics", organizationId] });
      void queryClient.invalidateQueries({ queryKey: ["knowledge", organizationId] });
      void queryClient.invalidateQueries({ queryKey: ["approvals", organizationId] });
      void queryClient.invalidateQueries({ queryKey: ["runs", organizationId] });
      void queryClient.invalidateQueries({ queryKey: ["usage", organizationId] });
    },
  });
}
