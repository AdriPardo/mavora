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

export type Company = {
  id: string;
  name: string;
  websiteUrl: string | null;
  description: string | null;
  market: string | null;
  products: Array<{ id: string; name: string; description: string | null; url: string | null }>;
  updatedAt: string;
};

export type Goal = {
  id: string;
  metric: string;
  targetValue: number;
  deadline: string;
  budgetCents: number;
  budgetCurrency: string;
  market: string;
  status: string;
  createdAt: string;
};

export type Strategy = {
  id: string;
  status: string;
  positioning: string;
  icpSummary: string;
  channelsJson: string;
  pillarsJson: string;
  kpisJson: string;
  narrative: string;
  createdAt: string;
};

export type Workflow = {
  id: string;
  type: string;
  status: string;
  errorMessage: string | null;
  createdAt: string;
  finishedAt: string | null;
};

export type AgentRun = {
  id: string;
  workflowId: string;
  agentType: string;
  status: string;
  model: string | null;
  promptTokens: number;
  completionTokens: number;
  costCents: number;
  errorMessage: string | null;
  startedAt: string;
  finishedAt: string | null;
};

export type Usage = {
  spentCentsThisMonth: number;
  monthlyBudgetCents: number;
};

export type Workspace = {
  company: Company | null;
  goal: Goal | null;
  strategy: Strategy | null;
  pendingApprovals: number;
  knowledgeCount: number;
  contentCount: number;
  campaignCount: number;
  publicationCount: number;
  workflows: Workflow[];
  runs: AgentRun[];
  usage: Usage;
};

export type Approval = {
  id: string;
  type: string;
  subjectType: string;
  subjectId: string;
  status: string;
  summary: string;
  createdAt: string;
  decidedAt: string | null;
  decisionNote: string | null;
};

export type KnowledgeItem = {
  id: string;
  kind: string;
  title: string;
  body: string;
  source: string | null;
  confidence: number | null;
  createdAt: string;
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

export function orgPath(organizationId: string, suffix: string): string {
  return `/api/v1/organizations/${organizationId}${suffix}`;
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

export function fetchWorkspace(organizationId: string, signal?: AbortSignal): Promise<Workspace> {
  return request<Workspace>(orgPath(organizationId, "/workspace"), { signal });
}

export function upsertCompany(
  organizationId: string,
  input: {
    name: string;
    websiteUrl?: string;
    description?: string;
    market?: string;
    products: Array<{ name: string; description?: string; url?: string }>;
  },
): Promise<Company> {
  return request<Company>(orgPath(organizationId, "/company"), {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function ingestWebsite(organizationId: string): Promise<KnowledgeItem> {
  return request<KnowledgeItem>(orgPath(organizationId, "/company/website-ingest"), { method: "POST" });
}

export function createGoal(
  organizationId: string,
  input: {
    metric: string;
    targetValue: number;
    deadline: string;
    budgetCents: number;
    budgetCurrency?: string;
    market: string;
  },
): Promise<Goal> {
  return request<Goal>(orgPath(organizationId, "/goals"), {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function enqueueWorkflow(organizationId: string, type: string): Promise<Workflow> {
  return request<Workflow>(orgPath(organizationId, `/workflows/${type}`), { method: "POST" });
}

export function fetchStrategy(organizationId: string, signal?: AbortSignal): Promise<Strategy> {
  return request<Strategy>(orgPath(organizationId, "/strategy"), { signal });
}

export function fetchApprovals(organizationId: string, signal?: AbortSignal): Promise<{ items: Approval[] }> {
  return request<{ items: Approval[] }>(orgPath(organizationId, "/approvals"), { signal });
}

export function decideApproval(
  organizationId: string,
  approvalId: string,
  approved: boolean,
  note?: string,
): Promise<Approval> {
  return request<Approval>(orgPath(organizationId, `/approvals/${approvalId}/decide`), {
    method: "POST",
    body: JSON.stringify({ approved, note }),
  });
}

export function fetchKnowledge(organizationId: string, signal?: AbortSignal): Promise<{ items: KnowledgeItem[] }> {
  return request<{ items: KnowledgeItem[] }>(orgPath(organizationId, "/knowledge"), { signal });
}

export function fetchAgentRuns(organizationId: string, signal?: AbortSignal): Promise<{ items: AgentRun[] }> {
  return request<{ items: AgentRun[] }>(orgPath(organizationId, "/agent-runs"), { signal });
}

export function fetchUsage(organizationId: string, signal?: AbortSignal): Promise<Usage> {
  return request<Usage>(orgPath(organizationId, "/usage"), { signal });
}

export type Research = {
  personas: Array<{ id: string; name: string; summary: string; pains: string; jobs: string }>;
  competitors: Array<{ id: string; name: string; url: string | null; notes: string }>;
  icp: { id: string; summary: string; segmentsJson: string } | null;
};

export function fetchResearch(organizationId: string, signal?: AbortSignal): Promise<Research> {
  return request<Research>(orgPath(organizationId, "/research"), { signal });
}

export type ContentBoard = {
  ideas: Array<{ id: string; title: string; angle: string; pillar: string | null; status: string }>;
  pieces: Array<{
    id: string;
    title: string;
    body: string;
    status: string;
    variants: Array<{ id: string; channel: string; body: string }>;
  }>;
};

export function fetchContent(organizationId: string, signal?: AbortSignal): Promise<ContentBoard> {
  return request<ContentBoard>(orgPath(organizationId, "/content"), { signal });
}

export type Campaign = { id: string; name: string; status: string; channelsJson: string; createdAt: string };
export type Publication = {
  id: string;
  pieceId: string | null;
  channel: string;
  copy: string;
  status: string;
  publishedAt: string | null;
  createdAt: string;
};

export function fetchCampaigns(organizationId: string, signal?: AbortSignal): Promise<{ items: Campaign[] }> {
  return request<{ items: Campaign[] }>(orgPath(organizationId, "/campaigns"), { signal });
}

export function fetchPublications(organizationId: string, signal?: AbortSignal): Promise<{ items: Publication[] }> {
  return request<{ items: Publication[] }>(orgPath(organizationId, "/publications"), { signal });
}

export function publishPublication(organizationId: string, publicationId: string): Promise<Publication> {
  return request<Publication>(orgPath(organizationId, `/publications/${publicationId}/publish`), { method: "POST" });
}

export type AnalyticsBoard = {
  snapshots: Array<{ id: string; metric: string; value: number; capturedAt: string; source: string }>;
  insights: Array<{ id: string; title: string; body: string; createdAt: string }>;
  learnings: Array<{ id: string; insightId: string | null; title: string; body: string; createdAt: string }>;
};

export function fetchAnalytics(organizationId: string, signal?: AbortSignal): Promise<AnalyticsBoard> {
  return request<AnalyticsBoard>(orgPath(organizationId, "/analytics"), { signal });
}

export function recordSnapshot(
  organizationId: string,
  input: { metric: string; value: number; source?: string },
): Promise<AnalyticsBoard["snapshots"][number]> {
  return request(orgPath(organizationId, "/analytics/snapshots"), {
    method: "POST",
    body: JSON.stringify(input),
  });
}
