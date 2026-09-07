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

export type InstagramStatus = {
  provider: string;
  connected: boolean;
  username: string | null;
  igUserId: string | null;
  autonomyEnabled: boolean;
  connectedAt: string | null;
  professionalAccountRequired: boolean;
  oauthReady: boolean;
};

export type MetaSetup = {
  appId: string;
  secretConfigured: boolean;
  redirectUri: string;
  graphVersion: string;
  oauthReady: boolean;
  source: "none" | "organization" | "environment" | string;
  publicApiUrl: string;
  publicAppUrl: string;
  suggestedRedirectUri: string;
  scopes: string[];
  developerConsoleUrl: string;
};

export type BrandBrief = {
  voice: string | null;
  offer: string | null;
  cta: string | null;
  audience: string | null;
  extraNotes: string | null;
  updatedAt: string | null;
};

export type MediaAsset = {
  id: string;
  kind: string;
  filename: string;
  contentType: string;
  byteSize: number;
  captionHint: string | null;
  productId: string | null;
  url: string;
  createdAt: string;
};

export type InstagramSlot = {
  id: string;
  format: string;
  status: string;
  scheduledAt: string;
  hook: string;
  caption: string;
  cta: string;
  hashtags: string[];
  mediaAssetIds: string[];
  igMediaId: string | null;
  errorMessage: string | null;
  publishedAt: string | null;
};

export type InstagramPlaybook = {
  timezone: string;
  principles: string;
  mix: Array<{ day: string; time: string; format: string }>;
  llmProvider: string;
  mediaProvider: string;
};

export function fetchInstagram(organizationId: string, signal?: AbortSignal): Promise<InstagramStatus> {
  return request<InstagramStatus>(orgPath(organizationId, "/instagram"), { signal });
}

export function fetchMetaSetup(organizationId: string, signal?: AbortSignal): Promise<MetaSetup> {
  return request<MetaSetup>(orgPath(organizationId, "/instagram/meta"), { signal });
}

export function saveMetaSetup(
  organizationId: string,
  input: { appId: string; appSecret?: string; redirectUri?: string; graphVersion?: string },
): Promise<MetaSetup> {
  return request<MetaSetup>(orgPath(organizationId, "/instagram/meta"), {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function clearMetaSetup(organizationId: string): Promise<MetaSetup> {
  return request<MetaSetup>(orgPath(organizationId, "/instagram/meta"), { method: "DELETE" });
}

export function connectInstagramToken(
  organizationId: string,
  input: { username: string; igUserId: string; pageId?: string; accessToken: string },
): Promise<InstagramStatus> {
  return request<InstagramStatus>(orgPath(organizationId, "/instagram/connect-token"), {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function connectInstagramFake(organizationId: string, username: string): Promise<InstagramStatus> {
  return request<InstagramStatus>(orgPath(organizationId, "/instagram/connect-fake"), {
    method: "POST",
    body: JSON.stringify({ username }),
  });
}

export function fetchInstagramConnectUrl(organizationId: string): Promise<{ url: string; provider: string }> {
  return request(orgPath(organizationId, "/instagram/connect-url"));
}

export function setInstagramAutonomy(organizationId: string, autonomyEnabled: boolean): Promise<InstagramStatus> {
  return request<InstagramStatus>(orgPath(organizationId, "/instagram/autonomy"), {
    method: "PATCH",
    body: JSON.stringify({ autonomyEnabled }),
  });
}

export function disconnectInstagram(organizationId: string): Promise<InstagramStatus> {
  return request<InstagramStatus>(orgPath(organizationId, "/instagram/disconnect"), { method: "POST" });
}

export function fetchBrandBrief(organizationId: string, signal?: AbortSignal): Promise<BrandBrief> {
  return request<BrandBrief>(orgPath(organizationId, "/instagram/brief"), { signal });
}

export function upsertBrandBrief(
  organizationId: string,
  input: {
    voice?: string;
    offer?: string;
    cta?: string;
    audience?: string;
    extraNotes?: string;
  },
): Promise<BrandBrief> {
  return request<BrandBrief>(orgPath(organizationId, "/instagram/brief"), {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function fetchMedia(organizationId: string, signal?: AbortSignal): Promise<{ items: MediaAsset[] }> {
  return request(orgPath(organizationId, "/media"), { signal });
}

export async function uploadMedia(
  organizationId: string,
  file: File,
  captionHint?: string,
): Promise<MediaAsset> {
  const body = new FormData();
  body.append("file", file);
  if (captionHint) {
    body.append("captionHint", captionHint);
  }
  const response = await fetch(`${apiBase}${orgPath(organizationId, "/media")}`, {
    method: "POST",
    body,
    credentials: "include",
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new ApiError(
      response.status,
      typeof payload.title === "string" ? payload.title : undefined,
      typeof payload.detail === "string" ? payload.detail : undefined,
    );
  }
  return payload as MediaAsset;
}

export function deleteMedia(organizationId: string, assetId: string): Promise<void> {
  return request<void>(orgPath(organizationId, `/media/${assetId}`), { method: "DELETE" });
}

export function fetchInstagramSlots(
  organizationId: string,
  signal?: AbortSignal,
): Promise<{ items: InstagramSlot[] }> {
  return request(orgPath(organizationId, "/instagram/slots"), { signal });
}

export function fetchInstagramPlaybook(organizationId: string, signal?: AbortSignal): Promise<InstagramPlaybook> {
  return request(orgPath(organizationId, "/instagram/playbook"), { signal });
}

export function generateInstagramWeek(organizationId: string): Promise<Workflow> {
  return request<Workflow>(orgPath(organizationId, "/instagram/week"), { method: "POST" });
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
