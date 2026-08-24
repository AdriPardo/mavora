import { Navigate, Outlet, useLocation } from "react-router-dom";
import { type ReactNode } from "react";
import { useAuth } from "@/features/auth/auth-provider";

export function RequireAuth() {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="flex min-h-dvh items-center justify-center text-sm text-zinc-500">
        Cargando…
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}

export function RedirectIfAuthenticated({ children }: { children: ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();
  if (isLoading) {
    return (
      <div className="flex min-h-dvh items-center justify-center text-sm text-zinc-500">
        Cargando…
      </div>
    );
  }
  if (isAuthenticated) {
    return <Navigate to="/overview" replace />;
  }
  return children;
}
