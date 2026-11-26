import type { ReactNode } from 'react'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'

/** Pergunta antes de algo que não dá para desfazer (excluir, cancelar venda). */
export function ConfirmarAcao({
  aberto,
  aoFechar,
  titulo,
  descricao,
  confirmar,
  aoConfirmar,
  perigo = true,
  desabilitado,
  children,
}: {
  aberto: boolean
  aoFechar: () => void
  titulo: string
  descricao?: ReactNode
  confirmar: string
  aoConfirmar: () => void
  perigo?: boolean
  desabilitado?: boolean
  children?: ReactNode
}) {
  return (
    <AlertDialog open={aberto} onOpenChange={(v) => !v && aoFechar()}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{titulo}</AlertDialogTitle>
          {descricao && <AlertDialogDescription>{descricao}</AlertDialogDescription>}
        </AlertDialogHeader>
        {children}
        <AlertDialogFooter>
          <AlertDialogCancel size="lg">Voltar</AlertDialogCancel>
          <AlertDialogAction
            size="lg"
            disabled={desabilitado}
            className={perigo ? 'bg-destructive text-white hover:bg-destructive/90' : undefined}
            onClick={aoConfirmar}
          >
            {confirmar}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
