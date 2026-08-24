import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { useQueryClient } from "@tanstack/react-query";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { ApiError, createGoal, ingestWebsite, upsertCompany } from "@/lib/api";

const schema = z.object({
  name: z.string().min(2, "Mínimo 2 caracteres").max(120),
  websiteUrl: z.string().max(2048).optional().or(z.literal("")),
  description: z.string().max(4000).optional().or(z.literal("")),
  market: z.string().max(200).optional().or(z.literal("")),
  productName: z.string().min(2, "Añade un producto").max(120),
  productDescription: z.string().max(2000).optional().or(z.literal("")),
  metric: z.string().min(2).max(80),
  targetValue: z.coerce.number().int().positive(),
  deadline: z.string().min(1, "Indica una fecha"),
  budgetEuros: z.coerce.number().positive(),
  goalMarket: z.string().min(2).max(200),
});

type Values = z.infer<typeof schema>;

export function OnboardingForm({ organizationId }: { organizationId: string }) {
  const queryClient = useQueryClient();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const form = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: "",
      websiteUrl: "",
      description: "",
      market: "",
      productName: "",
      productDescription: "",
      metric: "signups",
      targetValue: 100,
      deadline: "",
      budgetEuros: 1500,
      goalMarket: "España",
    },
  });

  async function onSubmit(values: Values) {
    setSubmitError(null);
    try {
      await upsertCompany(organizationId, {
        name: values.name,
        websiteUrl: values.websiteUrl || undefined,
        description: values.description || undefined,
        market: values.market || undefined,
        products: [{ name: values.productName, description: values.productDescription || undefined }],
      });
      await createGoal(organizationId, {
        metric: values.metric,
        targetValue: values.targetValue,
        deadline: values.deadline,
        budgetCents: Math.round(values.budgetEuros * 100),
        budgetCurrency: "EUR",
        market: values.goalMarket,
      });
      if (values.websiteUrl) {
        try {
          await ingestWebsite(organizationId);
        } catch (error) {
          setSubmitError(
            error instanceof ApiError
              ? `Empresa y objetivo creados. El website no se pudo leer: ${error.message}`
              : "Empresa y objetivo creados. El website no se pudo leer.",
          );
        }
      }
      await queryClient.invalidateQueries({ queryKey: ["workspace", organizationId] });
    } catch (error) {
      setSubmitError(error instanceof ApiError ? error.message : "No se pudo guardar el onboarding");
    }
  }

  return (
    <form className="max-w-xl space-y-4" onSubmit={form.handleSubmit(onSubmit)} noValidate>
      <p className="text-sm leading-6 text-zinc-500 dark:text-zinc-400">
        Primero la empresa y un objetivo medible. El CMO no improvisará un plan genérico.
      </p>
      <Field label="Nombre de la empresa" error={form.formState.errors.name?.message}>
        <Input {...form.register("name")} />
      </Field>
      <Field label="Website (opcional)" error={form.formState.errors.websiteUrl?.message}>
        <Input placeholder="https://" {...form.register("websiteUrl")} />
      </Field>
      <Field label="Descripción" error={form.formState.errors.description?.message}>
        <Textarea {...form.register("description")} />
      </Field>
      <Field label="Mercado de la empresa" error={form.formState.errors.market?.message}>
        <Input placeholder="SaaS B2B en España" {...form.register("market")} />
      </Field>
      <Field label="Producto principal" error={form.formState.errors.productName?.message}>
        <Input {...form.register("productName")} />
      </Field>
      <Field label="Métrica del objetivo" error={form.formState.errors.metric?.message}>
        <Input {...form.register("metric")} />
      </Field>
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label="Target" error={form.formState.errors.targetValue?.message}>
          <Input type="number" min={1} {...form.register("targetValue")} />
        </Field>
        <Field label="Fecha límite" error={form.formState.errors.deadline?.message}>
          <Input type="date" {...form.register("deadline")} />
        </Field>
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label="Presupuesto (EUR)" error={form.formState.errors.budgetEuros?.message}>
          <Input type="number" min={1} step="1" {...form.register("budgetEuros")} />
        </Field>
        <Field label="Mercado del objetivo" error={form.formState.errors.goalMarket?.message}>
          <Input {...form.register("goalMarket")} />
        </Field>
      </div>
      {submitError ? <p className="text-sm text-red-700 dark:text-red-400">{submitError}</p> : null}
      <Button type="submit" disabled={form.formState.isSubmitting}>
        {form.formState.isSubmitting ? "Guardando…" : "Guardar y continuar"}
      </Button>
    </form>
  );
}

function Field({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <label className="block space-y-1.5">
      <span className="text-sm font-medium">{label}</span>
      {children}
      {error ? <span className="block text-xs text-red-700 dark:text-red-400">{error}</span> : null}
    </label>
  );
}
