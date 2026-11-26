import { useId, type ReactNode } from 'react'
import { Label } from '@/components/ui/label'
import { cn } from '@/lib/utils'

/**
 * Rótulo, controle, dica e erro, com os ids de acessibilidade ligados. O controle recebe
 * as props por render-prop: <Campo rotulo="Nome" erro={...}>{(p) => <Input {...p} />}</Campo>
 */
export type PropsControle = {
  id: string
  'aria-invalid'?: boolean
  'aria-describedby'?: string
}

export function Campo({
  rotulo,
  erro,
  dica,
  opcional,
  className,
  children,
}: {
  rotulo: ReactNode
  erro?: string
  dica?: ReactNode
  opcional?: boolean
  className?: string
  children: (props: PropsControle) => ReactNode
}) {
  const id = useId()
  const idDica = dica ? `${id}-dica` : undefined
  const idErro = erro ? `${id}-erro` : undefined
  const descritores = [idErro, idDica].filter(Boolean).join(' ') || undefined
  return (
    <div className={cn('space-y-2', className)}>
      <Label htmlFor={id}>
        {rotulo}
        {opcional && <span className="font-normal text-muted-foreground">(opcional)</span>}
      </Label>
      {children({ id, 'aria-invalid': erro ? true : undefined, 'aria-describedby': descritores })}
      {dica && !erro && (
        <p id={idDica} className="text-sm text-muted-foreground">
          {dica}
        </p>
      )}
      {erro && (
        <p id={idErro} className="text-sm text-destructive">
          {erro}
        </p>
      )}
    </div>
  )
}
