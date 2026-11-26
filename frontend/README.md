# Frontend

Aplicação web do Gestão Vendas: Vite + React 19 + TypeScript + Tailwind 4 + shadcn/ui.
Usado quase só no celular (o dono administra pelo PC). Todas as telas da etapa 8 estão prontas; falta só a de compras, que depende da etapa 5 (ver `docs/ROADMAP.md`).

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

## Telas (`src/pages`)

| Pasta | Telas |
|-------|-------|
| `pages/` | `Login`, `EscolherNegocio`, `Inicio`, `Mais` (menu do celular), `NaoEncontrada` |
| `pages/cadastros/` | `Produtos` + `FormProduto`, `Categorias`, `Clientes` + `FormCliente` + `DetalheCliente`, `Veiculos`, `Usuarios` |
| `pages/operacao/` | `NovaVenda`, `Vendas`, `DetalheVenda`, `NovaTroca`, `Trocas`, `DetalheTroca`, `Perdas` |
| `pages/estoque/` | `Estoque`, `ExtratoProduto` (com "Informar estoque") |
| `pages/despesas/` | `Despesas` + `FormDespesa` |
| `pages/relatorios/` | `Relatorios`, `SeletorPeriodo`, `Graficos` (Recharts) |

As rotas ficam em `src/router.tsx`. Cada tela carrega sob demanda (`lazy`), e o `handle` da rota dá o título da aba e pode esconder a barra inferior (`semBarra`, nas telas de lançamento).

## Peças para montar uma tela nova

| Peça | Onde | Para quê |
|------|------|----------|
| `Pagina`, `Secao`, `Cartao` | `components/pagina/Pagina.tsx` | Cabeçalho (voltar, título, ações) e superfícies |
| `TabelaDados` | `components/lista/TabelaDados.tsx` | TanStack Table v9: tabela no PC e um cartão por linha no celular. As colunas (`colunasDe<T>()`, de `lib/tabela.ts`) ficam no escopo do módulo |
| `usePaginado` | `hooks/use-paginado.ts` | `useInfiniteQuery` sobre o `PaginaDTO`, com "Carregar mais" (`CarregarMais`) |
| `EstadosLista` | `components/lista/EstadosLista.tsx` | Carregando, erro com "tentar de novo", vazio e conteúdo |
| `CampoBusca`, `Filtros`, `FiltroPeriodo` | `components/lista/` | Busca com debounce (`useAtraso`) e filtros em chips |
| `PainelFormulario` | `components/formulario/PainelFormulario.tsx` | Formulário que sobe de baixo no celular e abre na lateral no PC |
| `Campo`, `InputNumero`, `Selecao`, `Pilulas`, `Contador` | `components/formulario/` | Campos com rótulo, erro e acessibilidade; números com teclado decimal |
| `BarraAcao` | `components/formulario/BarraAcao.tsx` | Total e botão principal fixos no rodapé (venda, troca) |
| `SeletorProduto`, `SeletorCliente` | `components/seletores/` | Busca de produto (com saldo) e de cliente (com cadastro rápido) |
| `useAcao` | `hooks/use-acao.ts` | Mutação com toast de sucesso/erro e invalidação das listas |
| `ConfirmarAcao` | `components/ConfirmarAcao.tsx` | Confirmação antes de excluir ou cancelar |
| `useCarrinho` | `lib/carrinho.ts` | Rascunho da venda por negócio e último preço de cada produto (Zustand, salvo no aparelho) |
| `paraNumero`, `paraCampo`, `campoNumero` | `lib/numeros.ts` | Texto pt-BR ("1.234,56") ↔ número, e validação Zod |
| `baixarArquivo` | `lib/api/arquivo.ts` | PDF com token: compartilhar no celular, baixar no PC |

## Convenções

- Import com alias: `@/lib/...`, `@/components/...`.
- Dinheiro chega como número (BigDecimal no backend); formate só na exibição.
- Datas: `LocalDate` chega como `AAAA-MM-DD` e `LocalDateTime` sem fuso, já no horário de Brasília. Use `formatarData`/`formatarDataHora` e, para enviar, `hojeIso()`/`agoraIso()`; nunca `toISOString()` (converte para UTC).
- Consultas de dados do negócio levam o `negocioId` na `queryKey` (ex.: `['produtos', negocioId, filtros]`) e o header com `doNegocio(negocioId)`: o OpenAPI marca o `X-Negocio-Id` como obrigatório.
- Cores: use os tokens (`bg-primary`, `bg-accent`, `text-muted-foreground`, `faixa-marca`...), nunca cores fixas; assim a tela segue o negócio e o tema.
- Mobile-first: botões `size="lg"` (48 px) nas ações principais, `Input` com 48 px e texto de 16 px (o iPhone não dá zoom).
- Formulários: React Hook Form + Zod, com os números guardados como texto e convertidos no envio (`paraNumero`). Para ler um campo no render, use `useWatch` (o `form.watch` atrapalha o React Compiler).
- Depois de uma mutação, invalide as chaves afetadas (ex.: venda → `['vendas', n]`, `['estoque', n]`, `['dashboard', n]`).
- Nada de `transition-all`: liste as propriedades (`transition-[background-color,transform]`).
- `dados()` para respostas com corpo; `semConteudo()` para DELETE/204.
- Listagens paginadas devolvem `{ conteudo, pagina, tamanho, totalElementos, totalPaginas }`.
- Permissões: o FUNCIONARIO registra venda, perda, troca e despesa e cadastra clientes;
  o resto é do ADMIN (`tipoUsuario` no login). Esconda o que ele não pode usar.
