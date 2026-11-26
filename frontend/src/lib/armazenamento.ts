import { createJSONStorage, type StateStorage } from 'zustand/middleware'

/**
 * localStorage que não derruba a tela: navegação privada, armazenamento cheio ou bloqueado
 * lançam exceção. Nesses casos guarda em memória (vale até recarregar a página).
 */
const memoria = new Map<string, string>()

const seguro: StateStorage = {
  getItem: (chave) => {
    try {
      return localStorage.getItem(chave)
    } catch {
      return memoria.get(chave) ?? null
    }
  },
  setItem: (chave, valor) => {
    try {
      localStorage.setItem(chave, valor)
    } catch {
      memoria.set(chave, valor)
    }
  },
  removeItem: (chave) => {
    try {
      localStorage.removeItem(chave)
    } catch {
      memoria.delete(chave)
    }
  },
}

export const armazenamento = createJSONStorage(() => seguro)
