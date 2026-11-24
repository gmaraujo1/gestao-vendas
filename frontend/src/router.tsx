import { createBrowserRouter } from 'react-router'
import { Inicio } from '@/pages/Inicio'
import { NaoEncontrada } from '@/pages/NaoEncontrada'

/** Rotas do app. Próximas: /login, /negocio, /vendas, /estoque, /clientes, /despesas... */
export const router = createBrowserRouter([
  { path: '/', element: <Inicio /> },
  { path: '*', element: <NaoEncontrada /> },
])
