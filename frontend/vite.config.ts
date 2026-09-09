import { defineConfig, type Plugin } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import fs from "node:fs";
import path from "node:path";
import type { IncomingMessage, ServerResponse } from "node:http";
import type { Connect } from "vite";

function privacyHtmlRoutes(): Plugin {
  const routes = new Set(["/privacidad", "/privacy", "/privacidad/", "/privacy/"]);
  const htmlPath = path.resolve(__dirname, "public/privacidad.html");

  function attach(middlewares: Connect.Server) {
    middlewares.use((req: IncomingMessage, res: ServerResponse, next: Connect.NextFunction) => {
      const url = req.url?.split("?")[0] ?? "";
      if (!routes.has(url)) {
        next();
        return;
      }
      res.setHeader("Content-Type", "text/html; charset=utf-8");
      res.setHeader("Cache-Control", "no-cache");
      fs.createReadStream(htmlPath).pipe(res);
    });
  }

  return {
    name: "privacy-html-routes",
    configureServer(server) {
      attach(server.middlewares);
    },
    configurePreviewServer(server) {
      attach(server.middlewares);
    },
  };
}

export default defineConfig({
  plugins: [privacyHtmlRoutes(), react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
    },
  },
  server: {
    port: 5173,
    proxy: {
      "/api": { target: "http://127.0.0.1:8080", changeOrigin: true },
      "/actuator": { target: "http://127.0.0.1:8080", changeOrigin: true },
    },
  },
});
