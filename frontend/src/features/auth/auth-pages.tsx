import { zodResolver } from "@hookform/resolvers/zod";
import { type ReactNode, useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/features/auth/auth-provider";
import { ApiError } from "@/lib/api";

const schema = z.object({
  email: z.string().email("Email inválido"),
  password: z.string().min(1, "Introduce tu contraseña"),
});

type FormValues = z.infer<typeof schema>;

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: "", password: "" },
  });

  async function onSubmit(values: FormValues) {
    setSubmitError(null);
    try {
      await login(values);
      const from = (location.state as { from?: string } | null)?.from;
      navigate(from && from !== "/login" ? from : "/overview", { replace: true });
    } catch (error) {
      setSubmitError(error instanceof ApiError ? error.message : "No se pudo iniciar sesión");
    }
  }

  return (
    <AuthScreen
      title="Entrar"
      subtitle="Accede al centro de operaciones de tu organización."
    >
      <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)} noValidate>
        <Field label="Email" error={form.formState.errors.email?.message}>
          <Input type="email" autoComplete="email" {...form.register("email")} />
        </Field>
        <Field label="Contraseña" error={form.formState.errors.password?.message}>
          <Input type="password" autoComplete="current-password" {...form.register("password")} />
        </Field>
        {submitError ? <p className="text-sm text-red-700 dark:text-red-400">{submitError}</p> : null}
        <Button type="submit" className="w-full" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Entrando…" : "Entrar"}
        </Button>
      </form>
      <p className="mt-6 text-sm text-zinc-500">
        ¿No tienes cuenta?{" "}
        <Link className="font-medium text-zinc-900 dark:text-zinc-100" to="/register">
          Crear organización
        </Link>
      </p>
    </AuthScreen>
  );
}

const registerSchema = z.object({
  organizationName: z.string().min(2, "Mínimo 2 caracteres").max(120),
  email: z.string().email("Email inválido"),
  password: z.string().min(10, "Mínimo 10 caracteres").max(72),
});

type RegisterValues = z.infer<typeof registerSchema>;

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const form = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { organizationName: "", email: "", password: "" },
  });

  async function onSubmit(values: RegisterValues) {
    setSubmitError(null);
    try {
      await register(values);
      navigate("/overview", { replace: true });
    } catch (error) {
      setSubmitError(error instanceof ApiError ? error.message : "No se pudo crear la cuenta");
    }
  }

  return (
    <AuthScreen
      title="Crear organización"
      subtitle="El registro crea tu usuario y el workspace. Después conectarás la empresa."
    >
      <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)} noValidate>
        <Field label="Nombre de la organización" error={form.formState.errors.organizationName?.message}>
          <Input autoComplete="organization" {...form.register("organizationName")} />
        </Field>
        <Field label="Email" error={form.formState.errors.email?.message}>
          <Input type="email" autoComplete="email" {...form.register("email")} />
        </Field>
        <Field label="Contraseña" error={form.formState.errors.password?.message}>
          <Input type="password" autoComplete="new-password" {...form.register("password")} />
        </Field>
        {submitError ? <p className="text-sm text-red-700 dark:text-red-400">{submitError}</p> : null}
        <Button type="submit" className="w-full" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Creando…" : "Crear cuenta"}
        </Button>
      </form>
      <p className="mt-6 text-sm text-zinc-500">
        ¿Ya tienes cuenta?{" "}
        <Link className="font-medium text-zinc-900 dark:text-zinc-100" to="/login">
          Entrar
        </Link>
      </p>
    </AuthScreen>
  );
}

function AuthScreen({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle: string;
  children: ReactNode;
}) {
  return (
    <div className="flex min-h-dvh items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <p className="mb-8 text-sm font-semibold tracking-tight">Mavora</p>
        <h1 className="text-xl font-semibold tracking-tight">{title}</h1>
        <p className="mt-1 text-sm leading-6 text-zinc-500 dark:text-zinc-400">{subtitle}</p>
        <div className="mt-8">{children}</div>
      </div>
    </div>
  );
}

function Field({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: ReactNode;
}) {
  return (
    <label className="block space-y-1.5">
      <span className="text-sm font-medium">{label}</span>
      {children}
      {error ? <span className="block text-xs text-red-700 dark:text-red-400">{error}</span> : null}
    </label>
  );
}
