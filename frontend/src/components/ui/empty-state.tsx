import { cn } from "@/lib/utils";

type EmptyStateProps = {
  title: string;
  description: string;
  className?: string;
};

export function EmptyState({ title, description, className }: EmptyStateProps) {
  return (
    <div
      className={cn(
        "flex min-h-48 flex-col justify-center rounded-lg border border-dashed border-zinc-200 px-6 py-10 dark:border-zinc-800",
        className,
      )}
    >
      <h2 className="text-sm font-medium text-zinc-900 dark:text-zinc-100">{title}</h2>
      <p className="mt-1 max-w-lg text-sm leading-6 text-zinc-500 dark:text-zinc-400">
        {description}
      </p>
    </div>
  );
}
