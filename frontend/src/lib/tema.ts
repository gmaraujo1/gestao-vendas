import { useSyncExternalStore } from 'react'
import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { armazenamento } from '@/lib/armazenamento'

/**
 * Tema claro, escuro ou automático (segue o celular). Zustand salvo em gv.tema; o script do
 * index.html aplica a classe .dark antes da primeira pintura.
 */
export type Tema = 'light' | 'dark' | 'system'

export const CHAVE_TEMA = 'gv.tema'

export const useTemaStore = create<{ tema: Tema; setTema: (tema: Tema) => void }>()(
  persist((set) => ({ tema: 'system', setTema: (tema) => set({ tema }) }), {
    name: CHAVE_TEMA,
    storage: armazenamento,
    partialize: ({ tema }) => ({ tema }),
  }),
)

const CONSULTA_ESCURO = '(prefers-color-scheme: dark)'

function assinarSistema(aoMudar: () => void) {
  const consulta = matchMedia(CONSULTA_ESCURO)
  consulta.addEventListener('change', aoMudar)
  return () => consulta.removeEventListener('change', aoMudar)
}

/** O tema em uso de fato ("system" resolvido pela preferência do celular). */
export function useTemaResolvido(): 'light' | 'dark' {
  const tema = useTemaStore((s) => s.tema)
  const sistemaEscuro = useSyncExternalStore(assinarSistema, () => matchMedia(CONSULTA_ESCURO).matches)
  return tema === 'system' ? (sistemaEscuro ? 'dark' : 'light') : tema
}
