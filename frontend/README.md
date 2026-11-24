# Frontend

Aplicação web do Gestão Vendas: Vite + React 19 + TypeScript + Tailwind 4 + shadcn/ui.
As telas ainda não foram feitas (etapa 8 do `docs/ROADMAP.md`); este projeto tem só a base configurada.

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
| Sessão | `src/lib/sessao.ts`, `src/lib/sessao-acoes.ts` | Token, login e negócio escolhido (localStorage). Pela interface use `sair()` e `trocarNegocio()`, que limpam o cache. Um 401 chama `sair()` sozinho. |
| React Query | `src/lib/query-client.ts` | Sem retry em erro 4xx; devtools em dev. |
| Rotas | `src/router.tsx` | React Router (só a página inicial provisória por enquanto). |
| Formatação | `src/lib/formatadores.ts` | Moeda (R$), quantidade, data e data/hora em pt-BR. |
| UI | `src/components/ui/` | Componentes shadcn: `npx shadcn@latest add input table dialog ...` |

Bibliotecas já instaladas para as telas: TanStack Query e Table, React Hook Form + Zod
(`@hookform/resolvers`), Recharts e lucide-react (ícones).

## Convenções

- Import com alias: `@/lib/...`, `@/components/...`.
- Dinheiro chega como número (BigDecimal no backend); formate só na exibição.
- Datas: `LocalDate` chega como `AAAA-MM-DD` e `LocalDateTime` sem fuso, já no horário de Brasília. Use `formatarData`/`formatarDataHora` e, para enviar, `hojeIso()`/`agoraIso()`; nunca `toISOString()` (converte para UTC).
- Consultas de dados do negócio levam o `negocioId` na `queryKey` (ex.: `['produtos', negocioId, filtros]`).
- `dados()` para respostas com corpo; `semConteudo()` para DELETE/204.
- Listagens paginadas devolvem `{ conteudo, pagina, tamanho, totalElementos, totalPaginas }`.
- Permissões: o FUNCIONARIO registra venda, perda, troca e despesa e cadastra clientes;
  o resto é do ADMIN (`tipoUsuario` no login). Esconda o que ele não pode usar.
