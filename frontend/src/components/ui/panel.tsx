import { type ReactNode } from "react";
import { cn } from "@/lib/utils";

export function Panel({
  title,
  children,
  className,
}: {
  title: string;
  children: ReactNode;
  className?: string;
}) {
  return (
    <section
      className={cn(
        "rounded-lg border border-zinc-200 bg-white p-5 dark:border-zinc-800 dark:bg-zinc-950",
        className,
      )}
    >
      <h2 className="text-sm font-medium text-zinc-900 dark:text-zinc-100">{title}</h2>
      <div className="mt-2 text-sm leading-6 text-zinc-600 dark:text-zinc-400">{children}</div>
    </section>
  );
}

export function StatusBadge({ value }: { value: string }) {
  return (
    <span className="inline-flex rounded-full border border-zinc-200 px-2 py-0.5 text-xs font-medium uppercase tracking-wide text-zinc-600 dark:border-zinc-800 dark:text-zinc-300">
      {value}
    </span>
  );
}

export function euros(cents: number, currency = "EUR"): string {
  return new Intl.NumberFormat("es-ES", { style: "currency", currency }).format(cents / 100);
}

export function parseJsonList(raw: string | null | undefined): string[] {
  if (!raw) return [];
  try {
    const value = JSON.parse(raw) as unknown;
    if (Array.isArray(value)) {
      return value.map((item) => (typeof item === "string" ? item : JSON.stringify(item)));
    }
  } catch {
    return [];
  }
  return [];
}
