import {
  ArrowLeftRight,
  BarChart3,
  Car,
  Home,
  LayoutGrid,
  Monitor,
  Moon,
  Package,
  PackageX,
  Receipt,
  ShoppingBasket,
  Sun,
  Tags,
  UserCog,
  Users,
  Wallet,
  type LucideIcon,
} from 'lucide-react'
import type { Tema } from '@/lib/tema'

export type ItemMenu = {
  rota: string
  rotulo: string
  icone: LucideIcon
  /** Só o ADMIN vê (as regras da API estão no SecurityConfig do backend). */
  soAdmin?: boolean
}

export type GrupoMenu = { titulo: string; itens: ItemMenu[] }

export const NOVA_VENDA: ItemMenu = { rota: '/vendas/nova', rotulo: 'Nova venda', icone: ShoppingBasket }

/** Barra inferior do celular (a "Nova venda" fica no meio, em destaque). */
export const BARRA_INFERIOR: ItemMenu[] = [
  { rota: '/', rotulo: 'Início', icone: Home },
  { rota: '/vendas', rotulo: 'Vendas', icone: Receipt },
  { rota: '/estoque', rotulo: 'Estoque', icone: Package },
  { rota: '/mais', rotulo: 'Mais', icone: LayoutGrid },
]

/** Menu completo: tela "Mais" no celular e menu lateral no computador. */
export const GRUPOS_MENU: GrupoMenu[] = [
  {
    titulo: 'Dia a dia',
    itens: [
      { rota: '/', rotulo: 'Início', icone: Home },
      { rota: '/vendas', rotulo: 'Vendas', icone: Receipt },
      { rota: '/trocas', rotulo: 'Trocas', icone: ArrowLeftRight },
      { rota: '/perdas', rotulo: 'Perdas', icone: PackageX },
      { rota: '/estoque', rotulo: 'Estoque', icone: Package },
      { rota: '/despesas', rotulo: 'Despesas', icone: Wallet },
    ],
  },
  {
    titulo: 'Cadastros',
    itens: [
      { rota: '/clientes', rotulo: 'Clientes', icone: Users },
      { rota: '/produtos', rotulo: 'Produtos', icone: ShoppingBasket },
      { rota: '/categorias', rotulo: 'Categorias', icone: Tags, soAdmin: true },
      { rota: '/veiculos', rotulo: 'Veículos', icone: Car, soAdmin: true },
    ],
  },
  {
    titulo: 'Gestão',
    itens: [
      { rota: '/relatorios', rotulo: 'Relatórios', icone: BarChart3, soAdmin: true },
      { rota: '/usuarios', rotulo: 'Usuários', icone: UserCog, soAdmin: true },
    ],
  },
]

export function visiveis(itens: ItemMenu[], ehAdmin: boolean) {
  return itens.filter((i) => ehAdmin || !i.soAdmin)
}

/** Opções de aparência (menu da conta e tela Mais). */
export const OPCOES_TEMA: { valor: Tema; rotulo: string; icone: LucideIcon }[] = [
  { valor: 'light', rotulo: 'Claro', icone: Sun },
  { valor: 'dark', rotulo: 'Escuro', icone: Moon },
  { valor: 'system', rotulo: 'Automático', icone: Monitor },
]

