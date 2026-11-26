import { useShallow } from 'zustand/react/shallow'
import { useSessaoStore } from '@/lib/sessao'

/** Login, negócio escolhido e perfil. A tela atualiza ao entrar, sair ou trocar de negócio. */
export function useSessao() {
  return useSessaoStore(
    useShallow((s) => ({
      login: s.login,
      negocioId: s.negocioId,
      marca: s.marca,
      ehAdmin: s.login?.tipoUsuario === 'ADMIN',
    })),
  )
}
