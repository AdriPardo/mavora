import {
  BarChart3,
  Brain,
  CheckSquare,
  Compass,
  FileText,
  LayoutDashboard,
  Megaphone,
  Plug,
  Search,
  Settings,
  Users,
} from "lucide-react";
import { NavLink } from "react-router-dom";
import { cn } from "@/lib/utils";
import { useAuth } from "@/features/auth/auth-provider";

export const NAV_ITEMS = [
  { to: "/overview", label: "Overview", icon: LayoutDashboard },
  { to: "/strategy", label: "Estrategia", icon: Compass },
  { to: "/research", label: "Investigación", icon: Search },
  { to: "/content", label: "Contenido", icon: FileText },
  { to: "/campaigns", label: "Campañas", icon: Megaphone },
  { to: "/analytics", label: "Analítica", icon: BarChart3 },
  { to: "/knowledge", label: "Knowledge", icon: Brain },
  { to: "/approvals", label: "Aprobaciones", icon: CheckSquare },
  { to: "/team", label: "Equipo", icon: Users },
  { to: "/integrations", label: "Integraciones", icon: Plug },
  { to: "/settings", label: "Ajustes", icon: Settings },
] as const;

type SidebarProps = {
  onNavigate?: () => void;
};

export function Sidebar({ onNavigate }: SidebarProps) {
  const { organization, account } = useAuth();

  return (
    <div className="flex h-full flex-col">
      <div className="flex h-14 flex-col justify-center px-4">
        <span className="text-sm font-semibold tracking-tight">Mavora</span>
        {organization ? (
          <span className="truncate text-xs text-zinc-500 dark:text-zinc-400">{organization.name}</span>
        ) : null}
      </div>
      <nav className="flex-1 space-y-0.5 px-2 pb-4" aria-label="Principal">
        {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            onClick={onNavigate}
            className={({ isActive }) =>
              cn(
                "flex items-center gap-2 rounded-md px-2.5 py-2 text-sm text-zinc-600 hover:bg-zinc-100 hover:text-zinc-900 dark:text-zinc-400 dark:hover:bg-zinc-900 dark:hover:text-zinc-100",
                isActive &&
                  "bg-zinc-100 font-medium text-zinc-900 dark:bg-zinc-900 dark:text-zinc-50",
              )
            }
          >
            <Icon className="size-4 shrink-0" aria-hidden="true" />
            {label}
          </NavLink>
        ))}
      </nav>
      {account ? (
        <div className="border-t border-zinc-200 px-4 py-3 dark:border-zinc-800">
          <p className="truncate text-xs text-zinc-500 dark:text-zinc-400">{account.user.email}</p>
        </div>
      ) : null}
    </div>
  );
}
