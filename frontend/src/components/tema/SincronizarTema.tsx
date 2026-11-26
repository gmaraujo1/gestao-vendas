import { useLayoutEffect } from 'react'
import { atualizarCorBarra } from '@/lib/marca'
import { useTemaResolvido } from '@/lib/tema'

/** Aplica o tema no <html> (classe .dark) antes da pintura e acerta a cor da barra. */
export function SincronizarTema() {
  const tema = useTemaResolvido()
  useLayoutEffect(() => {
    const html = document.documentElement
    html.classList.toggle('dark', tema === 'dark')
    html.style.colorScheme = tema
    atualizarCorBarra()
  }, [tema])
  return null
}
