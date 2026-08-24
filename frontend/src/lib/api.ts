const apiBase = import.meta.env.VITE_API_BASE_URL ?? "";

export type HealthResponse = {
  status: string;
  service: string;
};

export async function fetchHealth(signal?: AbortSignal): Promise<HealthResponse> {
  const response = await fetch(`${apiBase}/api/v1/health`, {
    signal,
    headers: { Accept: "application/json" },
  });
  if (!response.ok) {
    throw new Error(`Health check failed (${response.status})`);
  }
  return (await response.json()) as HealthResponse;
}
