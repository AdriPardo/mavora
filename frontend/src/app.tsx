import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "@/components/layout/app-shell";
import { RedirectIfAuthenticated, RequireAuth } from "@/features/auth/auth-gates";
import { LoginPage, RegisterPage } from "@/features/auth/auth-pages";
import { OverviewPage } from "@/features/overview/overview-page";
import { StrategyPage } from "@/features/strategy/strategy-page";
import { ResearchPage } from "@/features/research/research-page";
import { ContentPage } from "@/features/content/content-page";
import { CampaignsPage } from "@/features/campaigns/campaigns-page";
import { AnalyticsPage } from "@/features/analytics/analytics-page";
import { KnowledgePage } from "@/features/knowledge/knowledge-page";
import { ApprovalsPage } from "@/features/approvals/approvals-page";
import { TeamPage } from "@/features/team/team-page";
import { SettingsPage } from "@/features/settings/settings-page";
import { IntegrationsPage } from "@/features/integrations/integrations-page";
import { InstagramPage } from "@/features/instagram/instagram-page";

export function App() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <RedirectIfAuthenticated>
            <LoginPage />
          </RedirectIfAuthenticated>
        }
      />
      <Route
        path="/register"
        element={
          <RedirectIfAuthenticated>
            <RegisterPage />
          </RedirectIfAuthenticated>
        }
      />
      <Route element={<RequireAuth />}>
        <Route element={<AppShell />}>
          <Route index element={<Navigate to="/overview" replace />} />
          <Route path="/overview" element={<OverviewPage />} />
          <Route path="/strategy" element={<StrategyPage />} />
          <Route path="/research" element={<ResearchPage />} />
          <Route path="/content" element={<ContentPage />} />
          <Route path="/campaigns" element={<CampaignsPage />} />
          <Route path="/analytics" element={<AnalyticsPage />} />
          <Route path="/knowledge" element={<KnowledgePage />} />
          <Route path="/approvals" element={<ApprovalsPage />} />
          <Route path="/team" element={<TeamPage />} />
          <Route path="/instagram" element={<InstagramPage />} />
          <Route path="/integrations" element={<IntegrationsPage />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/overview" replace />} />
    </Routes>
  );
}
