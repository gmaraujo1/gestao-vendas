import { lazy, StrictMode, Suspense } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router'
import { toast } from 'sonner'
import { SincronizarTema } from '@/components/tema/SincronizarTema'
import { Toaster } from '@/components/ui/sonner'
import { aoExpirarSessao } from '@/lib/api/client'
import { queryClient } from '@/lib/query-client'
import { useSessaoStore } from '@/lib/sessao'
import { iniciarEfeitosDaSessao } from '@/lib/sessao-efeitos'
import { router } from '@/router'
import './index.css'

// devtools só em dev: o import dinâmico não entra no build de produção
// (no celular o botão dele cobriria o cabeçalho: só aparece em telas largas)
const ReactQueryDevtools =
  import.meta.env.DEV && matchMedia('(min-width: 768px)').matches
    ? lazy(() =>
        import('@tanstack/react-query-devtools').then((m) => ({ default: m.ReactQueryDevtools })),
      )
    : () => null

iniciarEfeitosDaSessao()

// sessão vencida (401): os guardas de rota levam ao login; depois de entrar, volta para cá
aoExpirarSessao(() => {
  useSessaoStore.getState().expirar(router.state.location.pathname)
  toast.info('Sua sessão expirou. Entre de novo.', { id: 'sessao-expirada' })
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <SincronizarTema />
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
      <Toaster position="top-center" richColors closeButton />
      <Suspense>
        <ReactQueryDevtools initialIsOpen={false} buttonPosition="bottom-left" />
      </Suspense>
    </QueryClientProvider>
  </StrictMode>,
)
