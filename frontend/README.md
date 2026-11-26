# Frontend

Aplicação web do Gestão Vendas: Vite + React 19 + TypeScript + Tailwind 4 + shadcn/ui.
Usado quase só no celular. As telas saem por partes (etapa 8 do `docs/ROADMAP.md`): a 8.1 (login, escolha do negócio e layout) está pronta.

## Comandos

```bash
npm install
npm run dev        # http://localhost:5173 (o /api vai para o backend em localhost:8080)
npm run typecheck
npm run lint       # oxlint
npm run build
npm run gen:api    # regenera src/lib/api/schema.d.ts a partir do backend rodando
```

Para o `gen:api`, suba o backend com a documentação ligada:
`SPRINGDOC_ENABLED=true ./mvnw spring-boot:run` (em `backend/`). Rode de novo sempre que a API mudar.

## O que já está pronto

| Peça | Onde | Para quê |
|------|------|----------|
| Cliente da API tipado | `src/lib/api/client.ts` | `api.GET('/api/produto', ...)` com tipos gerados do OpenAPI. Põe `Authorization` e `X-Negocio-Id` sozinho. `dados()` converte erro em `ApiError`. |
| Sessão | `src/lib/sessao.ts` | Store **Zustand** (salvo em `gv.sessao`): `entrar`, `escolherNegocio`, `sairDoNegocio`, `sair`. Não navegue depois de sair: os guardas de rota levam à tela certa. `lib/sessao-efeitos.ts` limpa o cache do React Query ao trocar de usuário ou negócio (inclusive vindo de outra aba). Um 401 da sessão atual expira a sessão e, depois de entrar de novo, volta à mesma tela. |
| React Query | `src/lib/query-client.ts` | Sem retry em erro 4xx; devtools em dev. |
| Rotas | `src/router.tsx` | React Router. Guardas em `components/rotas/guardas.tsx`: `ExigeLogin`, `ExigeNegocio`, `ExigeAdmin`. |
| Sessão nas telas | `src/hooks/use-sessao.ts` | `useSessao()` devolve `login`, `negocioId`, `marca` e `ehAdmin`. |
| Negócios | `src/hooks/use-negocios.ts` | `useMeusNegocios()` e `useNegocioAtual()` (negócio + marca). |
| Tema e marca | `src/lib/tema.ts`, `src/lib/marca.ts`, `src/index.css` | Store Zustand do tema (`gv.tema`: claro/escuro/automático) e as cores de cada negócio (`data-negocio`). |
| Layout | `src/components/layout/` | Cabeçalho, barra inferior (celular), menu lateral (md+) e itens do menu (`navegacao.ts`). |
| Estados | `src/components/estados/` | `EstadoVazio` (vazio, erro, em construção) e `SemPermissao`. |
| Formatação | `src/lib/formatadores.ts` | Moeda (R$), quantidade, data e data/hora em pt-BR. |
| UI | `src/components/ui/` | Componentes shadcn: `npx shadcn@latest add input table dialog ...` |

Bibliotecas: **TanStack Query** (dados do servidor), **Zustand** (estado do cliente: sessão, tema e, depois, o carrinho da venda),
**TanStack Table** (listagens e tabelas dos dashboards), React Hook Form + Zod, Recharts e lucide-react.
Sempre versões estáveis (`npm view <pacote> dist-tags`: só a `latest`).

## Convenções

- Import com alias: `@/lib/...`, `@/components/...`.
- Dinheiro chega como número (BigDecimal no backend); formate só na exibição.
- Datas: `LocalDate` chega como `AAAA-MM-DD` e `LocalDateTime` sem fuso, já no horário de Brasília. Use `formatarData`/`formatarDataHora` e, para enviar, `hojeIso()`/`agoraIso()`; nunca `toISOString()` (converte para UTC).
- Consultas de dados do negócio levam o `negocioId` na `queryKey` (ex.: `['produtos', negocioId, filtros]`) e o header com `doNegocio(negocioId)`: o OpenAPI marca o `X-Negocio-Id` como obrigatório.
- Cores: use os tokens (`bg-primary`, `bg-accent`, `text-muted-foreground`, `faixa-marca`...), nunca cores fixas; assim a tela segue o negócio e o tema.
- Mobile-first: botões `size="lg"` (48 px) nas ações principais, `Input` com 48 px e texto de 16 px (o iPhone não dá zoom).
- `dados()` para respostas com corpo; `semConteudo()` para DELETE/204.
- Listagens paginadas devolvem `{ conteudo, pagina, tamanho, totalElementos, totalPaginas }`.
- Permissões: o FUNCIONARIO registra venda, perda, troca e despesa e cadastra clientes;
  o resto é do ADMIN (`tipoUsuario` no login). Esconda o que ele não pode usar.
