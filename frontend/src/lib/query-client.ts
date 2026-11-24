import { QueryClient } from '@tanstack/react-query'
import { ApiError } from '@/lib/api/erros'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      // não insiste em erro de regra/permissão; só em falha de rede/servidor
      retry: (tentativas, erro) =>
        !(erro instanceof ApiError && erro.status < 500) && tentativas < 2,
      refetchOnWindowFocus: false,
    },
  },
})
