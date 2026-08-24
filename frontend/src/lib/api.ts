const apiBase = import.meta.env.VITE_API_BASE_URL ?? "";

export type HealthResponse = {
  status: string;
  service: string;
};

export type CurrentAccount = {
  user: { id: string; email: string };
  organizations: Array<{
    id: string;
    name: string;
    slug: string;
    plan: string;
    status: string;
    role: string;
  }>;
};

export class ApiError extends Error {
  readonly status: number;
  readonly title?: string;
  readonly detail?: string;

  constructor(status: number, title: string | undefined, detail: string | undefined) {
    super(detail ?? title ?? `Request failed (${status})`);
    this.status = status;
    this.title = title;
    this.detail = detail;
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  if (!headers.has("Accept")) {
    headers.set("Accept", "application/json");
  }
  if (init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`${apiBase}${path}`, {
    ...init,
    headers,
    credentials: "include",
  });

  if (response.status === 204) {
    return undefined as T;
  }

  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new ApiError(
      response.status,
      typeof payload.title === "string" ? payload.title : undefined,
      typeof payload.detail === "string" ? payload.detail : undefined,
    );
  }
  return payload as T;
}

export function fetchHealth(signal?: AbortSignal): Promise<HealthResponse> {
  return request<HealthResponse>("/api/v1/health", { signal });
}

export function fetchMe(signal?: AbortSignal): Promise<CurrentAccount> {
  return request<CurrentAccount>("/api/v1/auth/me", { signal });
}

export function registerAccount(input: {
  email: string;
  password: string;
  organizationName: string;
}): Promise<CurrentAccount> {
  return request<CurrentAccount>("/api/v1/auth/register", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function loginAccount(input: { email: string; password: string }): Promise<CurrentAccount> {
  return request<CurrentAccount>("/api/v1/auth/login", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function logoutAccount(): Promise<void> {
  return request<void>("/api/v1/auth/logout", { method: "POST" });
}
