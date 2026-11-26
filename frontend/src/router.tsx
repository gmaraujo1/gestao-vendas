import type { ComponentType } from 'react'
import { createBrowserRouter, type RouteObject } from 'react-router'
import { AppLayout, type HandleRota } from '@/components/layout/AppLayout'
import { ExigeAdmin, ExigeLogin, ExigeNegocio } from '@/components/rotas/guardas'
import { CarregandoTela } from '@/components/estados/CarregandoTela'
import { ErroRota } from '@/components/estados/ErroRota'
import { EscolherNegocio } from '@/pages/EscolherNegocio'
import { Inicio } from '@/pages/Inicio'
import { Login } from '@/pages/Login'
import { Mais } from '@/pages/Mais'
import { NaoEncontrada } from '@/pages/NaoEncontrada'

/**
 * Cada tela é carregada só quando abre (lazy): o celular baixa menos na primeira vez e os
 * gráficos (Recharts) só vêm com os relatórios.
 */
function tela(
  path: string,
  titulo: string,
  carregar: () => Promise<ComponentType>,
  extra: Omit<HandleRota, 'titulo'> = {},
): RouteObject {
  return {
    path,
    handle: { titulo, ...extra } satisfies HandleRota,
    // o erro aparece dentro do layout (o menu continua lá)
    errorElement: <ErroRota />,
    lazy: async () => ({ Component: await carregar() }),
  }
}

export const router = createBrowserRouter([
  { path: '/login', element: <Login /> },
  {
    element: <ExigeLogin />,
    HydrateFallback: CarregandoTela,
    errorElement: <ErroRota />,
    children: [
      { path: '/negocio', element: <EscolherNegocio /> },
      {
        element: <ExigeNegocio />,
        children: [
          {
            element: <AppLayout />,
            children: [
              { index: true, element: <Inicio />, handle: { titulo: 'Início' } satisfies HandleRota },
              { path: 'mais', element: <Mais />, handle: { titulo: 'Menu' } satisfies HandleRota },
              // dia a dia
              tela('vendas', 'Vendas', () => import('@/pages/operacao/Vendas').then((m) => m.Vendas)),
              tela('vendas/nova', 'Nova venda', () => import('@/pages/operacao/NovaVenda').then((m) => m.NovaVenda), { semBarra: true }),
              tela('vendas/:id', 'Venda', () => import('@/pages/operacao/DetalheVenda').then((m) => m.DetalheVenda)),
              tela('trocas', 'Trocas', () => import('@/pages/operacao/Trocas').then((m) => m.Trocas)),
              tela('trocas/nova', 'Nova troca', () => import('@/pages/operacao/NovaTroca').then((m) => m.NovaTroca), { semBarra: true }),
              tela('trocas/:id', 'Troca', () => import('@/pages/operacao/DetalheTroca').then((m) => m.DetalheTroca)),
              tela('perdas', 'Perdas', () => import('@/pages/operacao/Perdas').then((m) => m.Perdas)),
              tela('estoque', 'Estoque', () => import('@/pages/estoque/Estoque').then((m) => m.Estoque)),
              tela('estoque/:id', 'Extrato', () => import('@/pages/estoque/ExtratoProduto').then((m) => m.ExtratoProduto)),
              tela('despesas', 'Despesas', () => import('@/pages/despesas/Despesas').then((m) => m.Despesas)),
              // cadastros que o funcionário também usa (ele só lê produtos)
              tela('clientes', 'Clientes', () => import('@/pages/cadastros/Clientes').then((m) => m.Clientes)),
              tela('clientes/:id', 'Cliente', () => import('@/pages/cadastros/DetalheCliente').then((m) => m.DetalheCliente)),
              tela('produtos', 'Produtos', () => import('@/pages/cadastros/Produtos').then((m) => m.Produtos)),
              {
                element: <ExigeAdmin />,
                children: [
                  tela('categorias', 'Categorias', () => import('@/pages/cadastros/Categorias').then((m) => m.Categorias)),
                  tela('veiculos', 'Veículos', () => import('@/pages/cadastros/Veiculos').then((m) => m.Veiculos)),
                  tela('usuarios', 'Usuários', () => import('@/pages/cadastros/Usuarios').then((m) => m.Usuarios)),
                  tela('relatorios', 'Relatórios', () => import('@/pages/relatorios/Relatorios').then((m) => m.Relatorios)),
                ],
              },
              { path: '*', element: <NaoEncontrada /> },
            ],
          },
        ],
      },
    ],
  },
  { path: '*', element: <NaoEncontrada /> },
])
