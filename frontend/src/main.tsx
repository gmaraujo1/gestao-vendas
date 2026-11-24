import { lazy, StrictMode, Suspense } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router'
import { aoExpirarSessao } from '@/lib/api/client'
import { queryClient } from '@/lib/query-client'
import { sair } from '@/lib/sessao-acoes'
import { router } from '@/router'
import './index.css'

// devtools só em dev: o import dinâmico não entra no build de produção
const ReactQueryDevtools = import.meta.env.DEV
  ? lazy(() =>
      import('@tanstack/react-query-devtools').then((m) => ({ default: m.ReactQueryDevtools })),
    )
  : () => null

// sessão vencida (401): limpa tudo e volta para o início (a futura tela de login)
aoExpirarSessao(() => {
  sair()
  void router.navigate('/')
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
      <Suspense>
        <ReactQueryDevtools initialIsOpen={false} />
      </Suspense>
    </QueryClientProvider>
  </StrictMode>,
)
