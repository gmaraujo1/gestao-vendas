import { createBrowserRouter } from 'react-router'
import { AppLayout, type HandleRota } from '@/components/layout/AppLayout'
import { ExigeAdmin, ExigeLogin, ExigeNegocio } from '@/components/rotas/guardas'
import { EmBreve } from '@/pages/EmBreve'
import { EscolherNegocio } from '@/pages/EscolherNegocio'
import { Inicio } from '@/pages/Inicio'
import { Login } from '@/pages/Login'
import { Mais } from '@/pages/Mais'
import { NaoEncontrada } from '@/pages/NaoEncontrada'

/** Telas que entram nas próximas partes da etapa 8 (ver docs/ROADMAP.md). */
const emBreve = (path: string, titulo: string) => ({
  path,
  element: <EmBreve titulo={titulo} />,
  handle: { titulo } satisfies HandleRota,
})

export const router = createBrowserRouter([
  { path: '/login', element: <Login /> },
  {
    element: <ExigeLogin />,
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
              emBreve('vendas', 'Vendas'),
              emBreve('vendas/nova', 'Nova venda'),
              emBreve('trocas', 'Trocas'),
              emBreve('perdas', 'Perdas'),
              emBreve('estoque', 'Estoque'),
              emBreve('despesas', 'Despesas'),
              emBreve('clientes', 'Clientes'),
              emBreve('produtos', 'Produtos'),
              {
                element: <ExigeAdmin />,
                children: [
                  emBreve('categorias', 'Categorias'),
                  emBreve('veiculos', 'Veículos'),
                  emBreve('relatorios', 'Relatórios'),
                  emBreve('usuarios', 'Usuários'),
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  { path: '*', element: <NaoEncontrada /> },
])
