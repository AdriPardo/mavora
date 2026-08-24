import { Monitor, Moon, Sun } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useTheme, type Theme } from "@/components/theme/theme-provider";

const OPTIONS: { value: Theme; label: string; icon: typeof Sun }[] = [
  { value: "light", label: "Claro", icon: Sun },
  { value: "dark", label: "Oscuro", icon: Moon },
  { value: "system", label: "Sistema", icon: Monitor },
];

export function ThemeToggle() {
  const { theme, setTheme } = useTheme();

  return (
    <div className="inline-flex rounded-md border border-zinc-200 p-0.5 dark:border-zinc-800" role="group" aria-label="Tema">
      {OPTIONS.map(({ value, label, icon: Icon }) => {
        const selected = theme === value;
        return (
          <Button
            key={value}
            variant="ghost"
            size="sm"
            aria-pressed={selected}
            onClick={() => setTheme(value)}
            className={
              selected
                ? "bg-zinc-100 text-zinc-900 dark:bg-zinc-800 dark:text-zinc-100"
                : ""
            }
          >
            <Icon className="size-3.5" aria-hidden="true" />
            {label}
          </Button>
        );
      })}
    </div>
  );
}
