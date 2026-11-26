import { Skeleton } from '@/components/ui/skeleton'

/** Esqueleto enquanto o app ou uma tela carrega. */
export function CarregandoTela() {
  return (
    <div role="status" aria-busy="true" className="mx-auto max-w-3xl space-y-4 px-4 pt-6">
      <span className="sr-only">Carregando…</span>
      <Skeleton className="h-10 w-48" />
      <Skeleton className="h-44 rounded-3xl" />
      <Skeleton className="h-24 rounded-3xl" />
    </div>
  )
}
