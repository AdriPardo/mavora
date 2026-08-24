import { Menu, X } from "lucide-react";
import { useState } from "react";
import { Outlet } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Sidebar } from "@/components/layout/sidebar";

export function AppShell() {
  const [open, setOpen] = useState(false);

  return (
    <div className="min-h-dvh lg:grid lg:grid-cols-[15rem_1fr]">
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-50 focus:rounded-md focus:bg-white focus:px-3 focus:py-2 focus:text-sm dark:focus:bg-zinc-900"
      >
        Saltar al contenido
      </a>

      <aside className="hidden border-r border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-950 lg:block">
        <div className="sticky top-0 h-dvh">
          <Sidebar />
        </div>
      </aside>

      {open ? (
        <div className="fixed inset-0 z-40 lg:hidden">
          <button
            type="button"
            className="absolute inset-0 bg-zinc-950/40"
            aria-label="Cerrar menú"
            onClick={() => setOpen(false)}
          />
          <div className="relative h-full w-64 bg-white dark:bg-zinc-950">
            <div className="flex h-14 items-center justify-between px-3">
              <span className="text-sm font-semibold">Mavora</span>
              <Button variant="ghost" size="icon" onClick={() => setOpen(false)} aria-label="Cerrar menú">
                <X className="size-4" />
              </Button>
            </div>
            <Sidebar onNavigate={() => setOpen(false)} />
          </div>
        </div>
      ) : null}

      <div className="flex min-w-0 flex-col">
        <div className="flex h-14 items-center border-b border-zinc-200 px-3 dark:border-zinc-800 lg:hidden">
          <Button variant="ghost" size="icon" onClick={() => setOpen(true)} aria-label="Abrir menú">
            <Menu className="size-4" />
          </Button>
          <span className="ml-2 text-sm font-semibold">Mavora</span>
        </div>
        <main id="contenido" className="mx-auto w-full max-w-6xl flex-1 px-4 py-8 sm:px-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
