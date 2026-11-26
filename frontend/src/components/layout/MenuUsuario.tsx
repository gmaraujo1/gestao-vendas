import { ArrowLeftRight, LogOut } from 'lucide-react'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { OPCOES_TEMA } from '@/components/layout/navegacao'
import { useMeusNegocios } from '@/hooks/use-negocios'
import { useSessao } from '@/hooks/use-sessao'
import { useSessaoStore } from '@/lib/sessao'
import { useTemaStore, type Tema } from '@/lib/tema'
import { iniciais } from '@/lib/texto'

/**
 * Avatar com o menu da conta: aparência, trocar de negócio e sair. Sair e trocar só mexem
 * na sessão; os guardas de rota levam à tela certa.
 */
export function MenuUsuario() {
  const { login, ehAdmin } = useSessao()
  const { data: negocios } = useMeusNegocios()
  const tema = useTemaStore((s) => s.tema)
  const setTema = useTemaStore((s) => s.setTema)

  return (
    <DropdownMenu>
      <DropdownMenuTrigger
        aria-label="Conta e aparência"
        className="grid size-10 place-items-center rounded-full bg-primary text-sm font-semibold text-primary-foreground outline-none focus-visible:ring-3 focus-visible:ring-ring/50"
      >
        {iniciais(login?.nome)}
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-64 p-2">
        <DropdownMenuLabel className="px-2 py-1.5">
          <span className="block truncate text-sm font-semibold text-foreground">{login?.nome}</span>
          <span className="text-xs font-normal text-muted-foreground">
            {ehAdmin ? 'Administrador' : 'Funcionário'}
          </span>
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuLabel className="px-2 pt-1 pb-0.5 text-xs font-medium text-muted-foreground">
          Aparência
        </DropdownMenuLabel>
        <DropdownMenuRadioGroup value={tema} onValueChange={(v) => setTema(v as Tema)}>
          {OPCOES_TEMA.map(({ valor, rotulo, icone: Icone }) => (
            <DropdownMenuRadioItem key={valor} value={valor} className="h-10">
              <Icone aria-hidden="true" /> {rotulo}
            </DropdownMenuRadioItem>
          ))}
        </DropdownMenuRadioGroup>
        <DropdownMenuSeparator />
        {(negocios?.length ?? 0) > 1 && (
          <DropdownMenuItem className="h-11" onSelect={() => useSessaoStore.getState().sairDoNegocio()}>
            <ArrowLeftRight aria-hidden="true" /> Trocar de negócio
          </DropdownMenuItem>
        )}
        <DropdownMenuItem variant="destructive" className="h-11" onSelect={() => useSessaoStore.getState().sair()}>
          <LogOut aria-hidden="true" /> Sair
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
