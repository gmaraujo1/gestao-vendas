import { useCallback, useSyncExternalStore } from 'react'

/** true enquanto a media query casar (ex.: '(min-width: 768px)'). */
export function useMidia(consulta: string) {
  const assinar = useCallback(
    (aoMudar: () => void) => {
      const mq = matchMedia(consulta)
      mq.addEventListener('change', aoMudar)
      return () => mq.removeEventListener('change', aoMudar)
    },
    [consulta],
  )
  return useSyncExternalStore(assinar, () => matchMedia(consulta).matches)
}

/** Computador ou tablet deitado: menu lateral, tabelas e painéis laterais. */
export const useTelaLarga = () => useMidia('(min-width: 768px)')
