import { useEffect, useState } from 'react'

/** O valor só muda depois de um tempo parado (busca enquanto digita, sem uma chamada por tecla). */
export function useAtraso<T>(valor: T, ms = 300) {
  const [atrasado, setAtrasado] = useState(valor)
  useEffect(() => {
    const t = setTimeout(() => setAtrasado(valor), ms)
    return () => clearTimeout(t)
  }, [valor, ms])
  return atrasado
}
