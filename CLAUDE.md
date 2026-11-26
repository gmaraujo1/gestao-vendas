# Gestão Vendas

Sistema de gestão feito para os pais do autor. Os dados de cada negócio ficam isolados (dois sistemas em um):
- **Laticínio**: o foco agora.
- **Pão Nino**: também é fornecedor. O módulo dele fica para depois, mas a estrutura multi-negócio já deve suportá-lo.

Usuários: os pais (ADMIN) e um funcionário (FUNCIONARIO). Clientes **não** acessam o sistema.

## Estrutura

```
backend/    Java 21, Spring Boot 3.5, Spring Data JPA, PostgreSQL, MapStruct, Lombok (Maven)
frontend/   Vite 8 + React 19 + TS 5.9 + Tailwind 4 + shadcn/ui + TanStack Query/Table + Zustand + RHF/Zod + Recharts + React Router (todas as telas prontas, menos compras; ver frontend/README.md)
mobile/     React Native, só depois que o frontend e o backend estiverem prontos
.claude/    skills do ECC (sem as de outras stacks/domínios) e agents revisores (java, database, security, react, typescript)
```

## Comandos

```bash
cd backend && ./mvnw test   # testes (o CI roda o mesmo em PRs para main)
cd frontend && npm run lint && npm run build   # o CI também roda
cd frontend && npm run gen:api   # regenera os tipos da API (backend rodando com SPRINGDOC_ENABLED=true)
```
Postgres local: `cd backend && docker compose up -d` (usuário/senha padrão `gestao`; exporte `DB_USERNAME` e `DB_PASSWORD`).
Nesta máquina o JDK 21 do sistema não tem `javac`. Use
`JAVA_HOME=~/.jdks/corretto-22.0.2 ./mvnw test` (ou instale o `openjdk-21-jdk`).

## Regras de negócio

- **Escopo:** tudo pertence a um negócio (Laticínio/Pão Nino), com dados isolados e interface própria.
- **Produto:** CRUD completo, com categorias próprias de cada negócio.
- **Cliente:** só cadastro, para saber quem comprou o quê (ex.: "Marcelo levou 100 bombons esta semana").
- **Venda:** cliente + itens (produto, quantidade, preço). Gera saída no estoque.
- **Compra:** fornecedor + itens. O preço de compra varia, então o custo médio é recalculado. **O formato do pedido ao fornecedor ainda depende de confirmação com a mãe; manter flexível.**
- **Troca:** o cliente devolve um produto (ex.: leite perto de vencer) e leva outro com validade melhor. O item devolvido volta ao estoque ou vira perda.
- **Perda:** produto furado, vencido etc. Gera saída com motivo.
- **Ajuste ("informar estoque"):** contagem real que corrige a diferença.
- **Estoque:** o saldo **é derivado de movimentações** (compra, venda, troca, perda, ajuste). Nunca editar a quantidade diretamente; assim disponível, vendido e perdido ficam coerentes entre si.
- **Veículos:** a Fiorino (usada pelos pais) e o caminhão (usado pelo funcionário).
- **Despesas:** gasolina, pedágio, pneus, mecânica… Podem ser ligadas a um veículo.
- **Relatórios PDF:** lucro e prejuízo (vendas − custo das mercadorias vendidas − despesas) por dia, semana, quinzena, mês, trimestre, semestre e ano.
- **Dashboard:** gráficos de vendas, compras, gastos e lucro.
- **Futuro:** avisos em tempo real via WhatsApp (vendas, perdas, trocas). Por isso, desenhar as movimentações como eventos de domínio.

## Decisões técnicas

- Dinheiro sempre em `BigDecimal` (`NUMERIC` no banco; custo médio com 4 casas).
- Migrations com Flyway em `backend/src/main/resources/db/migration` (`V<n>__descricao.sql`). Nunca editar uma migration já mergeada; criar outra. O Hibernate só valida (`ddl-auto: validate`).
- Login: `POST /api/auth/login` devolve um JWT HS256 (resource server do Spring Security). O claim `tipo` vira `ROLE_ADMIN`/`ROLE_FUNCIONARIO`; `/api/usuario/**` é só ADMIN. Senhas com BCrypt. Quem pede uma ação vem do token, **nunca do corpo da requisição**.
- Variáveis de ambiente: `JWT_SECRET` (obrigatória, ≥ 32 bytes), `JWT_EXPIRACAO` (padrão 12h), `FUSO_HORARIO` (padrão America/Sao_Paulo), `SPRINGDOC_ENABLED`, `ADMIN_EMAIL`/`ADMIN_SENHA`/`ADMIN_NOME` (criam o primeiro ADMIN se não houver usuários), `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. Os testes usam o perfil `test` (`src/test/resources/application-test.yaml`).
- Isolamento por negócio: tabelas de dados têm `negocio_id`. O endpoint declara um parâmetro `NegocioAtual`, resolvido do header `X-Negocio-Id` e validado contra `usuario_negocio` (403 sem vínculo, 400 sem header). Use cases recebem `negocioId` e **toda** consulta filtra por ele (`findByIdAndNegocioId`, nunca `findById`). `GET /api/negocio/meus` lista os negócios do usuário logado. Os negócios vêm da migration (LATICINIO, PAO_NINO).
- Permissões (`SecurityConfig`): todo GET é liberado para quem está logado. O FUNCIONARIO não vê relatórios nem dashboard. Ele faz POST de venda, perda, troca, despesa e cliente e PATCH de cliente. **Qualquer outra escrita em `/api/**` é só do ADMIN por padrão**; para liberar algo ao FUNCIONARIO, adicione a regra explicitamente.
- Estoque: o saldo é **sempre a soma de `movimentacao_estoque`** (sem tabela de saldo). Toda movimentação passa pelo `MovimentacaoService.registrar`, que aplica o sinal pelo tipo; antes de gravar, o use case chama `MovimentacaoService.travar(produtoIds)`. Movimentação não é editada nem apagada: corrige-se com estorno ou ajuste. O estoque pode ficar negativo (decisão dos donos).
- Venda guarda o custo do momento em `venda_item.custo_unitario` (base do CMV). Venda não é editada: cancela-se (estorno). Cliente obrigatório.
- Eventos de domínio (`common/evento`): publique com `ApplicationEventPublisher` dentro da transação; ouvintes usam `@TransactionalEventListener` (depois do commit). O WhatsApp vai usar isso.
- Datas: `LocalDateTime`/`LocalDate` no fuso do negócio (`America/Sao_Paulo`, definido no `main()` e no surefire). Use o `Clock` injetado para "agora". Filtros de período usam `Periodo` (`[de, ate]` inclusivo na API).
- Exceções ao isolamento: `veiculo` é global (os dois negócios usam os mesmos veículos; a despesa é que escolhe o negócio). Usuário, auth e negócio também não usam `NegocioAtual`.
- Convenções de API: REST sem verbo na URL (`POST /api/x`, `GET /api/x`, `PATCH /api/x/{id}`, `DELETE /api/x/{id}`). O PATCH é parcial (campos nulos não mudam). Listagens grandes são paginadas com `Pageable` e devolvem `PaginaDTO`. Cadastros desativáveis têm o campo `ativo`. Entidades novas estendem `EntidadeAuditavel` (datas automáticas). Busca com `like` usa `Like.escapar`. Os endpoints antigos de `/api/usuario` ainda usam verbos (`/criar`, `/listar`).
- OpenAPI (springdoc): `/v3/api-docs` e `/swagger-ui.html`, **desligados por padrão** (`SPRINGDOC_ENABLED=true` para ligar; ligado no perfil de teste). Endpoints com `NegocioAtual` ganham o header `X-Negocio-Id` no schema. Pageable leva `@ParameterObject`.
- Frontend: mobile-first (os pais usam no celular; o dono administra pelo PC). Estado do servidor no TanStack Query (o `negocioId` sempre na queryKey), estado do cliente no Zustand, listas com `TabelaDados` (TanStack Table: tabela no PC, cartões no celular). Cores só por tokens (mudam com o negócio e o tema). Sempre versões estáveis das bibliotecas.
- PDF gerado no backend (OpenPDF, pacote `org.openpdf`), para que o mobile reaproveite.
- Relatórios (`relatorio/`): SQL nativo só leitura em `RelatorioRepository`, agregado por dia e somado em Java (o total é sempre a soma dos pontos). Períodos com `TipoPeriodo`/`IntervaloRelatorio` (`?periodo=MES&data=` ou `?de=&ate=`). `/api/relatorio/**` e `/api/dashboard/**` são só do ADMIN.
- Testcontainers para testes de integração com Postgres: anotar a classe com `@IntegrationTest` e nomeá-la `*IntegrationTest` (o surefire só roda `*Test`). Requer Docker. O `testcontainers.version` está fixado em 1.21.4 por causa do Docker 29. Os testes compartilham o banco e a ordem das classes muda entre máquinas: não dependa de dados de outra classe e não use `deleteAll()` em tabelas com histórico (use `LimpezaBanco`). Para conferir, rode `./mvnw test -Dsurefire.runOrder=random`.
- Padrão atual por módulo: `domain/`, `application/` (um use case por ação), `controller/`, `repository/`, `dto/`, `mapper/`.
- Pacote base `br.com.gestao.gestaovendas`, artefato `gestao-vendas`, banco `gestao_vendas`.

## Pendências conhecidas

- Banco: a V1 parte de um banco vazio (`gestao_vendas`). Um banco antigo com tabelas criadas à mão precisa ser recriado.
- Antes de expor a API na internet: limitar tentativas de login (no proxy ou na aplicação) e usar HTTPS.
- O JWT vale até expirar (12h), mesmo se o usuário for removido. Avaliar expiração menor, refresh token ou checagem do usuário no banco.
- Faltam endpoints para trocar e redefinir senha e para desativar conta (um ADMIN não pode ser removido pela API).
- Isolamento por convenção: todo controller de dados de negócio deve receber `NegocioAtual`. Considerar um teste ArchUnit quando houver mais módulos.
- A listagem de usuários ainda não é paginada (produto e cliente já são).

## Roadmap

O roadmap detalhado, com status, checklist de cada etapa e decisões em aberto, está em **`docs/ROADMAP.md`**. Leia esse arquivo ao retomar o trabalho e atualize-o ao concluir cada etapa.

Resumo: 1. Base ✅ → 2. Cadastros ✅ → 3. Estoque e vendas ✅ → 4. Despesas ✅ → 5. Compras → 6. Relatórios PDF e dashboard ✅ → 7. Pronto para produção (= MVP do backend) → 8. Frontend ✅ → 9. WhatsApp, mobile e Pão Nino.

Cada etapa sai em uma branch e um PR, com testes. Revisar com o agent `java-reviewer` (ou `react-reviewer`/`typescript-reviewer` no front) antes do PR.

## Git

**O Claude não faz commit, push nem PR**: o autor faz tudo de git. Deixe as mudanças no working tree e sugira a mensagem de commit e o texto do PR. **Nunca** adicione `Co-Authored-By: Claude` nem "Generated with Claude Code".
