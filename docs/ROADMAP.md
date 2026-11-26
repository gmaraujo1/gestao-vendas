# Roadmap: MVP do backend

Documento para retomar o trabalho em qualquer máquina. O `CLAUDE.md`, na raiz, tem as regras de negócio e as decisões técnicas. Este arquivo tem **o que já foi feito, o que falta e em que ordem**.

> **Para retomar com o Claude Code em outro PC:** clone o repositório, abra o Claude Code na raiz e diga
> "leia o docs/ROADMAP.md e vamos continuar a próxima etapa". O `CLAUDE.md` é carregado automaticamente.

Última atualização: 26/11/2026 (etapa 8.1: identidade visual, login, escolha do negócio e layout).

---

## Visão geral

| # | Etapa | Status | Branch sugerida |
|---|-------|--------|-----------------|
| 1 | Base do backend | ✅ Concluída (PRs #7 e #8) | — |
| 2 | Cadastros: categorias, produtos, clientes, veículos | ✅ Concluída (PR #10) | — |
| 3 | Estoque e vendas: movimentações, venda, perda, troca, ajuste | ✅ Concluída (PR #11) | — |
| 4 | Despesas | ✅ Concluída (PR #11) | — |
| 5 | Compras (custo médio) | ⏳ Próxima no backend; perguntas enviadas à mãe | `feat/compras` |
| 6 | Relatórios PDF e API do dashboard | ✅ Concluída (PR #12) | — |
| 7 | Pronto para produção | ⬜ | `chore/producao` |
| | **→ MVP do backend concluído** | | |
| 8 | Frontend (Vite + React) | ⏳ 8.1 feita (acesso e layout); faltam 8.2 a 8.5 | `feat/frontend-*` |
| 9 | Depois do MVP: WhatsApp, app mobile, módulo Pão Nino | ⬜ | — |

Regra de todas as etapas: uma branch e um PR por etapa, com testes (unitários e de integração com Testcontainers). Antes do PR, revisar com os agents `java-reviewer` e, se a etapa mexer em segurança, `security-reviewer`. Todo dado de negócio tem `negocio_id`, e todo endpoint de dados recebe `NegocioAtual`.

---

## ✅ Etapa 1: base do backend (concluída)

- Pacote `br.com.gestao.gestaovendas`, artefato `gestao-vendas`, banco `gestao_vendas`.
- Flyway: `V1__schema_inicial.sql` (usuario, produto) e `V2__negocio.sql` (negocio, usuario_negocio, produto.negocio_id).
- Dinheiro em `BigDecimal` (`NUMERIC`). Custo médio com 4 casas.
- Login com JWT (`POST /api/auth/login`), senhas com BCrypt, `/api/usuario/**` só para ADMIN.
- Primeiro ADMIN criado pelas variáveis `ADMIN_EMAIL` e `ADMIN_SENHA`, com acesso aos dois negócios.
- Isolamento por negócio: header `X-Negocio-Id`, validado contra `usuario_negocio` (403 sem vínculo, 400 sem header). `GET /api/negocio/meus`.
- Testcontainers (Postgres real nos testes), fixado na versão 1.21.4 por causa do Docker 29.
- 43 testes.

---

## ✅ Etapa 2: cadastros (concluída)

Migration `V3__cadastros.sql`. Todos os endpoints de negócio exigem `X-Negocio-Id`.

- **Categoria** (`/api/categoria`): tabela por negócio, no lugar do enum fixo.
  - O Laticínio já vem com Leites, Queijos, Iogurtes, Bebidas lácteas, Doces e Outros.
  - O nome é único no negócio, sem diferenciar maiúsculas.
  - Pode ser desativada. Não é excluída se tiver produto (409).
- **Produto** (`/api/produto`):
  - `categoriaId`, que precisa ser do mesmo negócio e ativa;
  - campo `ativo`;
  - listagem paginada (`?page=&size=&sort=`) com filtros por categoria, ativo, descrição, preço e datas;
  - PATCH parcial;
  - `POST /api/produto` no lugar de `/api/produto/cadastrar`.
- **Cliente** (`/api/cliente`): nome, telefone, endereço, cidade, observação e ativo.
  - Busca paginada por parte do nome.
  - PATCH parcial.
  - GET por id.
- **Veículo** (`/api/veiculo`): **global**, sem `X-Negocio-Id`, porque os dois negócios usam os mesmos veículos.
  - Descrição, placa (opcional, normalizada, única) e ativo.
  - Fiorino e Caminhão já vêm cadastrados.
  - Leitura para todos; alteração só pelo ADMIN.
- **Permissões:**
  - todos consultam os cadastros;
  - o FUNCIONARIO só cadastra e edita **clientes**;
  - produtos, categorias, veículos e qualquer exclusão são do ADMIN.
- **Garantias no banco:** uma FK composta `(categoria_id, negocio_id)` impede produto com categoria de outro negócio, mesmo que algum use case futuro esqueça a validação.
- **Listagens:**
  - `?sort=` só aceita campos de uma lista (outro campo devolve 400);
  - sempre desempatam por `id`, para a paginação não repetir nem pular registros com nome igual.
- **PATCH:** texto vazio (`""`) limpa campos opcionais; os textos são gravados sem espaços nas pontas.
- **Infraestrutura:**
  - `EntidadeAuditavel` preenche `criado_em`/`atualizado_em` sozinha;
  - `PaginaDTO` padroniza as respostas paginadas;
  - `?sort=` inválido devolve 400.
- **Decisões tomadas:**
  - as unidades continuam UN, CX e L;
  - o cliente não tem CPF;
  - nomes de cliente podem se repetir.
- **Fica para a etapa 3:** cliente e produto com movimentação não poderão ser excluídos, só desativados. A FK da venda garante isso.
- 73 testes.

---

## ✅ Etapa 3: estoque e vendas (concluída)

Migration `V4__estoque_vendas_despesas.sql`.

**Decisões dos donos:**
- O cliente é obrigatório na venda.
- O estoque pode ficar negativo.
- Na troca, quem registra escolhe se o devolvido volta ao estoque ou vira perda.
- O FUNCIONARIO registra venda, perda, troca e despesa. Cancelar venda e ajustar o estoque são só do ADMIN.

**O que foi feito:**
- **Movimentação de estoque** (`movimentacao_estoque`):
  - é imutável e tem sinal: + entra, − sai;
  - tipos: `VENDA`, `VENDA_ESTORNO`, `PERDA`, `TROCA_ENTRADA`, `TROCA_SAIDA`, `AJUSTE` e `COMPRA` (reservado para a etapa 5);
  - guarda o custo médio do produto no momento;
  - **o saldo é a soma das movimentações**, sem tabela de saldo;
  - toda gravação passa pelo `MovimentacaoService`, que aplica o sinal pelo tipo e trava os produtos (`pg_advisory_xact_lock`, em ordem de id) para o ajuste não brigar com uma venda simultânea.
- **Estoque** (`/api/estoque`):
  - saldo de todos os produtos, paginado e com filtros;
  - `GET /api/estoque/produto/{id}`: saldo e extrato;
  - `POST /api/estoque/ajuste` ("informar estoque", só ADMIN): grava a diferença entre a contagem e o saldo.
- **Venda** (`/api/venda`):
  - cliente obrigatório e itens (produto, quantidade, preço);
  - cada item guarda o **custo do momento** (base do CMV da etapa 6);
  - o subtotal arredonda HALF_UP;
  - `dataHora` opcional, que pode ser retroativa mas não futura;
  - listagem por período, cliente e status;
  - **cancelar** (`POST /api/venda/{id}/cancelar`, só ADMIN, com motivo) faz o estorno com o custo da venda original. A venda é travada no banco, então dois cliques não estornam duas vezes.
- **Perda** (`/api/perda`): produto, quantidade e motivo (`FURADO`, `VENCIDO`, `AVARIADO`, `OUTRO`). A listagem por período inclui as perdas vindas de trocas.
- **Troca** (`/api/troca`): cliente, itens devolvidos (destino `ESTOQUE` ou `PERDA` com motivo) e itens entregues.
  - Devolvido que vira perda gera `TROCA_ENTRADA` + `PERDA`: o estoque não muda, mas a perda aparece nos relatórios.
- **Eventos de domínio** (`common/evento`): `VendaRegistrada`, `VendaCancelada`, `PerdaRegistrada`, `TrocaRegistrada` e `EstoqueAjustado`.
  - Hoje só vão para o log, depois do commit.
  - O WhatsApp vai assinar esses eventos com `@TransactionalEventListener`.
- **Travas:**
  - produto, cliente ou veículo com histórico não é excluído (409; desative);
  - a unidade de um produto com movimentação não muda.
- **Fuso:** a JVM roda em `America/Sao_Paulo` (definido no `main()`, configurável por `FUSO_HORARIO`), para "vendas de hoje" fecharem no dia certo.

---

## ✅ Etapa 4: despesas (concluída)

- **Despesa** (`/api/despesa`, por negócio):
  - data (sem valor = hoje; não pode ser futura), valor, tipo (`GASOLINA`, `PEDAGIO`, `PNEU`, `MECANICA`, `OUTROS`), descrição e veículo opcional, que precisa estar ativo;
  - o FUNCIONARIO lança; editar e excluir é só do ADMIN.
- Listagem paginada por período, tipo e veículo.
- `GET /api/despesa/resumo?de=&ate=`: total e totais por tipo e por veículo.
- **OpenAPI (springdoc)**, adiantado da etapa 7:
  - `/v3/api-docs` e `/swagger-ui.html`, com JWT e header `X-Negocio-Id` documentados;
  - **desligado por padrão**: suba com `SPRINGDOC_ENABLED=true` para gerar o cliente do front.
- 100 testes, incluindo cancelamento simultâneo, isolamento do saldo entre negócios e permissões.

---

## Etapa 5: compras (custo médio)

**Bloqueio:** confirmar com a mãe o formato do pedido ao fornecedor (o que é anotado, se há nota, se o preço muda por item).

- [ ] **Fornecedor** (cadastro simples, por negócio). O Pão Nino também é fornecedor do Laticínio.
- [ ] **Compra**: fornecedor, data, itens (produto, quantidade, preço unitário pago) e total.
  - Cada item gera uma movimentação `COMPRA`.
  - **Recalcular o custo médio ponderado**: `(saldo × custo_atual + qtd × preço) / (saldo + qtd)`.
  - Histórico de preço pago por produto e fornecedor.
- [ ] Testes do custo médio com preços variando entre compras.

---

## ✅ Etapa 6: relatórios PDF e API do dashboard (concluída, PR #12)

Sem migration nova: tudo sai do que as etapas 3 e 4 já gravam. Módulo `relatorio/`.

**Período** (igual em todos os endpoints):
- `?periodo=DIA|SEMANA|QUINZENA|MES|TRIMESTRE|SEMESTRE|ANO&data=2026-11-10` (sem `data` = hoje);
- ou intervalo livre `?de=2026-11-01&ate=2026-11-20` (inclusivo, até 5 anos);
- semana de segunda a domingo; quinzena 1–15 e 16–fim do mês; trimestres e semestres alinhados ao ano.

**Cálculo** (`RelatorioRepository`, SQL nativo agregado por dia e somado em Java nas faixas):
- vendas = Σ subtotais das vendas `REGISTRADA` (a cancelada sai inteira);
- CMV = Σ (`quantidade × custo_unitario`) de cada item, **arredondado por item** para a soma dos dias bater com o total;
- lucro bruto = vendas − CMV; **resultado = lucro bruto − despesas** (positivo = lucro, negativo = prejuízo);
- perdas = Σ (|quantidade| × `custo_unitario`) das movimentações `PERDA` (avulsas e de trocas), mostradas à parte e também em `resultadoComPerdas`.

**Endpoints** (todos **só ADMIN**: lucro e custos são dos donos; ver decisão em aberto na etapa 7):
- `GET /api/relatorio/lucro`: resumo, detalhamento (por dia até 31 dias, por semana até 62, por mês até 2 anos), despesas por tipo, perdas por motivo e os 10 produtos que mais venderam (por valor);
- `GET /api/relatorio/lucro/pdf`: o mesmo em PDF A4 (OpenPDF), com `Content-Disposition` `lucro-laticinio-2026-11-01_2026-11-30.pdf`. No papel, as faixas sem lançamento são omitidas;
- `GET /api/dashboard/serie?agrupamento=`: pontos zerados inclusive, para o Recharts (máx. 400 pontos);
- `GET /api/dashboard/produtos-mais-vendidos?criterio=QUANTIDADE|VENDAS|LUCRO&limite=10`;
- `GET /api/dashboard/perdas`: custo por motivo e os produtos que mais se perderam;
- despesas por tipo e por veículo continuam em `GET /api/despesa/resumo`.

**Fica para depois:**
- gráfico de compras (etapa 5);
- custo líquido das trocas (produto entregue com custo diferente do devolvido) e o valor dos ajustes de contagem ainda não entram no resultado.

128 testes (cenário com valores conhecidos: venda cancelada, troca com perda, isolamento entre negócios, PDF lido de volta).

---

## Etapa 7: pronto para produção

Itens de segurança e operação que estão pendentes desde a etapa 1. Devem ficar prontos **antes de colocar o sistema na internet**.

- [x] **OpenAPI (springdoc)**: feito na etapa 4 (desligado por padrão; `SPRINGDOC_ENABLED=true` para ligar).
- [ ] **Limite de tentativas de login**: bloqueio após N falhas, por IP e por e-mail.
- [ ] **Senha**: endpoint para o usuário trocar a própria senha e para o ADMIN redefinir a de outro.
- [ ] **Desativar conta** (flag `ativo`) e invalidar o token de usuário desativado (checar no banco ou reduzir a expiração e usar refresh token).
- [ ] **Teste de arquitetura (ArchUnit)**: todo controller de dados de negócio recebe `NegocioAtual`.
- [ ] **Perfil de produção**: logs, `show-sql` desligado, CORS só para o domínio do front (em dev o proxy do Vite dispensa CORS).
- [ ] **Permissão de leitura**: hoje o FUNCIONARIO enxerga custos (`precoMedio`, `custoUnitario`) e totais de despesa. Decidir se isso fica só com o ADMIN. (Relatórios e dashboard já são só do ADMIN desde a etapa 6.)
- [ ] **Empacotamento**: Dockerfile do backend e `compose` de produção (backend + Postgres, com volume).
- [ ] **Backup do banco** (pg_dump agendado) e teste de restauração.
- [ ] **Deploy**: onde hospedar (VPS barata, Railway, Render…), HTTPS (Caddy/nginx) e variáveis de ambiente.

**→ Com as etapas 1 a 7 concluídas, o MVP do backend está pronto.**

---

## ⏳ Etapa 8: frontend (8.1 feita)

**Pronto** (veja `frontend/README.md`):
- Vite 8 + React 19 + TypeScript 5.9 + Tailwind 4 + shadcn/ui (preset Nova);
- TanStack Query/Table, React Hook Form + Zod, Recharts e React Router;
- cliente da API tipado (openapi-fetch + `npm run gen:api`) que manda o token e o `X-Negocio-Id` sozinho;
- sessão, formatadores pt-BR, proxy `/api` para o backend em dev;
- job de lint e build no CI.

**Identidade visual (decidida em 26/11/2026):**
- uso **quase só no celular**: mobile-first, alvos de toque de 40 a 48 px, barra inferior com a "Nova venda" no meio; menu lateral a partir de 768 px;
- tema claro, escuro ou automático (`gv.tema`), aplicado antes da primeira pintura por um script no `index.html`;
- **a cor muda com o negócio** (`data-negocio` no `<html>`, tokens no `src/index.css`):
  - Laticínio (marca Bomtlê): fundo branco, azul-marinho da logo, e os dois arcos do coração (vermelho e azul-céu) numa faixa fina no topo;
  - Pão Nino (ainda sem logo): trigo, casca tostada e marrom, com um selo de trigo;
  - login e escolha do negócio usam uma identidade neutra (grafite).

**Partes da etapa 8 (um PR cada):**
- [x] **8.1 Base e acesso** (`feat/frontend-base-acesso`): login, escolha do negócio (entra direto se houver um só), layout (cabeçalho, barra inferior, menu lateral, tela "Mais"), menu da conta (aparência, trocar negócio, sair), início com atalhos e, para o ADMIN, as vendas de hoje e o resultado do mês. As telas ainda não feitas mostram "em construção"; as do ADMIN mostram "só para administradores" ao FUNCIONARIO.
- [ ] **8.2 Cadastros**: produtos, categorias, clientes, veículos e usuários (ADMIN).
- [ ] **8.3 Operação**: venda rápida, lista e detalhe de vendas, cancelamento, troca e perda.
- [ ] **8.4 Estoque e despesas**: saldos, extrato, "informar estoque", despesas e resumo.
- [ ] **8.5 Dashboard e relatórios**: cards, gráficos (`/api/dashboard/*`), seletor de período e download do PDF.
- [ ] Depois da etapa 5: compras.

**Pendências do front:**
- o bundle passou de 500 kB: carregar as rotas com `lazy` quando entrarem as telas pesadas (gráficos na 8.5);
- PWA (instalar na tela inicial do celular) com manifest e ícones: avaliar na 8.5 ou na etapa 7.

## Etapa 9: depois do MVP

- **WhatsApp**: avisos em tempo real de vendas, perdas e trocas, assinando os eventos da etapa 3.
- **App React Native**: reusa a API e o cliente gerado.
- **Módulo Pão Nino**: a estrutura multi-negócio já suporta; falta definir as regras próprias dele.

---

## Decisões em aberto

| Assunto | Quem decide | Afeta |
|---------|-------------|-------|
| Formato do pedido ao fornecedor | Mãe | Etapa 5 |
| O FUNCIONARIO deve ver custos e totais de despesa? | Pais | Etapa 7 |
| Onde hospedar | Você | Etapa 7 |

---

## Ambiente de desenvolvimento (outro PC)

```bash
git clone <repo> && cd gestao-vendas/backend
docker compose up -d                 # Postgres local (usuário/senha: gestao)
export DB_USERNAME=gestao DB_PASSWORD=gestao
export JWT_SECRET="$(openssl rand -base64 48)"
export ADMIN_EMAIL=voce@exemplo.com ADMIN_SENHA=uma-senha-forte
./mvnw test                          # precisa de JDK 21 e Docker
SPRINGDOC_ENABLED=true ./mvnw spring-boot:run   # API em :8080, Swagger em /swagger-ui.html

# em outro terminal
cd ../frontend
npm install
npm run gen:api                      # tipos da API (backend precisa estar rodando)
npm run dev                          # http://localhost:5173
```

- Precisa de Node 22+ para o frontend.
- Precisa de JDK 21 **com `javac`** (por exemplo `openjdk-21-jdk`). No PC atual se usa `JAVA_HOME=~/.jdks/corretto-22.0.2`.
- As skills e os agents do Claude Code estão em `.claude/` e vêm junto no clone.
