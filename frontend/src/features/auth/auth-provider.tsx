import { createContext, useContext, type ReactNode } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  type CurrentAccount,
  ApiError,
  fetchMe,
  loginAccount,
  logoutAccount,
  registerAccount,
} from "@/lib/api";

type AuthContextValue = {
  account: CurrentAccount | undefined;
  isLoading: boolean;
  isAuthenticated: boolean;
  organization: CurrentAccount["organizations"][number] | undefined;
  login: (input: { email: string; password: string }) => Promise<void>;
  register: (input: {
    email: string;
    password: string;
    organizationName: string;
  }) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const me = useQuery({
    queryKey: ["me"],
    queryFn: async ({ signal }) => {
      try {
        return await fetchMe(signal);
      } catch (error) {
        if (error instanceof ApiError && error.status === 401) {
          return null;
        }
        throw error;
      }
    },
    retry: false,
  });

  const loginMutation = useMutation({
    mutationFn: loginAccount,
    onSuccess: (account) => {
      queryClient.setQueryData(["me"], account);
    },
  });

  const registerMutation = useMutation({
    mutationFn: registerAccount,
    onSuccess: (account) => {
      queryClient.setQueryData(["me"], account);
    },
  });

  const logoutMutation = useMutation({
    mutationFn: logoutAccount,
    onSuccess: () => {
      queryClient.setQueryData(["me"], null);
    },
  });

  const value: AuthContextValue = {
    account: me.data ?? undefined,
    isLoading: me.isLoading,
    isAuthenticated: Boolean(me.data),
    organization: me.data?.organizations[0],
    login: async (input) => {
      await loginMutation.mutateAsync(input);
    },
    register: async (input) => {
      await registerMutation.mutateAsync(input);
    },
    logout: async () => {
      await logoutMutation.mutateAsync();
    },
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
