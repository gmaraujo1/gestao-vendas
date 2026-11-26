import type { ReactNode } from 'react'
import { Sheet, SheetContent, SheetDescription, SheetFooter, SheetHeader, SheetTitle } from '@/components/ui/sheet'
import { useTelaLarga } from '@/hooks/use-midia'
import { cn } from '@/lib/utils'

/**
 * Painel dos formulários de cadastro: no celular sobe de baixo (o polegar alcança os botões);
 * no PC abre na lateral, sem tirar a lista de vista.
 */
export function PainelFormulario({
  aberto,
  aoFechar,
  titulo,
  descricao,
  children,
  rodape,
}: {
  aberto: boolean
  aoFechar: () => void
  titulo: string
  descricao?: string
  children: ReactNode
  rodape?: ReactNode
}) {
  const larga = useTelaLarga()
  return (
    <Sheet open={aberto} onOpenChange={(v) => !v && aoFechar()}>
      <SheetContent
        side={larga ? 'right' : 'bottom'}
        className={cn(
          'gap-0 p-0',
          larga ? 'w-full sm:max-w-md' : 'max-h-[92svh] rounded-t-3xl',
        )}
      >
        {!larga && <div className="mx-auto mt-2.5 h-1.5 w-10 shrink-0 rounded-full bg-muted-foreground/25" aria-hidden="true" />}
        <SheetHeader className="px-5 pt-4 pb-2 text-left">
          <SheetTitle className="text-lg">{titulo}</SheetTitle>
          {descricao && <SheetDescription>{descricao}</SheetDescription>}
        </SheetHeader>
        <div className="flex-1 overflow-y-auto px-5 pt-2 pb-4">{children}</div>
        {rodape && <SheetFooter className="pb-seguro border-t bg-popover px-5 pt-3">{rodape}</SheetFooter>}
      </SheetContent>
    </Sheet>
  )
}
