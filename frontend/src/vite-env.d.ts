/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** URL do backend. Vazio em dev: o proxy do Vite repassa /api para localhost:8080. */
  readonly VITE_API_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
