# Roadmap: MVP do backend

Documento para retomar o trabalho em qualquer máquina. O `CLAUDE.md`, na raiz, tem as regras de negócio e as decisões técnicas. Este arquivo tem **o que já foi feito, o que falta e em que ordem**.

> **Para retomar com o Claude Code em outro PC:** clone o repositório, abra o Claude Code na raiz e diga
> "leia o docs/ROADMAP.md e vamos continuar a próxima etapa". O `CLAUDE.md` é carregado automaticamente.

Última atualização: 24/11/2026 (etapa 2).

---

## Visão geral

| # | Etapa | Status | Branch sugerida |
|---|-------|--------|-----------------|
| 1 | Base do backend | ✅ Concluída (PRs #7 e #8) | — |
| 2 | Cadastros: categorias, produtos, clientes, veículos | ✅ Concluída (aguardando PR) | `feat/cadastros` |
| 3 | Estoque e vendas: movimentações, venda, perda, troca, ajuste | ⏳ Próxima | `feat/estoque-vendas` |
| 4 | Despesas | ⬜ | `feat/despesas` |
| 5 | Compras (custo médio) | ⬜ Depende da mãe | `feat/compras` |
| 6 | Relatórios PDF e API do dashboard | ⬜ | `feat/relatorios` |
| 7 | Pronto para produção | ⬜ | `chore/producao` |
| | **→ MVP do backend concluído** | | |
| 8 | Frontend (Vite + React) | ⬜ Pode começar após a etapa 4 | `feat/frontend-*` |
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

## Etapa 3: estoque e vendas (o coração do sistema)

**Objetivo:** registrar tudo o que entra e sai e derivar o saldo das movimentações.

- [ ] **Movimentação de estoque** (`movimentacao_estoque`): negócio, produto, tipo, quantidade com sinal, custo unitário do momento, data/hora, usuário, referência de origem (venda, troca…) e motivo.
  - Tipos: `VENDA`, `PERDA`, `TROCA_ENTRADA`, `TROCA_SAIDA`, `AJUSTE`, `COMPRA` (a compra entra na etapa 5).
  - **O saldo nunca é editado direto**: saldo = soma das movimentações. Pode haver uma tabela/visão de saldo atualizada na mesma transação, por performance.
  - Endpoint de saldo por produto e de extrato (histórico) de um produto.
  - Decidir se o estoque pode ficar negativo. Sugestão: bloquear a venda sem saldo, com aviso.
- [ ] **Venda**: cliente (opcional?), data, itens (produto, quantidade, preço unitário) e total.
  - Cada item gera uma movimentação `VENDA`.
  - O item guarda o **custo médio do momento** (necessário para calcular o lucro na etapa 6).
  - Listar com filtro por período e cliente ("o que o Marcelo levou esta semana").
  - Cancelar a venda gera um estorno, nunca apaga.
- [ ] **Perda**: produto, quantidade e motivo (`FURADO`, `VENCIDO`, `AVARIADO`, `OUTRO`), mais observação.
- [ ] **Troca**: cliente, item devolvido e item entregue.
  - O devolvido volta ao estoque (`TROCA_ENTRADA`) ou vira perda (`PERDA`, motivo VENCIDO etc.).
  - O entregue sai (`TROCA_SAIDA`).
- [ ] **Ajuste ("informar estoque")**: o usuário informa a contagem real e o sistema grava a diferença como `AJUSTE`.
- [ ] **Eventos de domínio**: `VendaRegistrada`, `PerdaRegistrada`, `TrocaRegistrada` publicados com `ApplicationEventPublisher`. Sem ouvinte por enquanto; o WhatsApp vai assinar depois.
- [ ] **Travar a edição do produto quando houver movimentação**: `unidadeMedida` não pode mudar (corromperia os saldos), e `precoMedio` deixa de ser editável à mão (passa a vir só das compras).
- [ ] **Fuso horário**: fixar `hibernate.jdbc.time_zone` (ou usar `Clock`) antes de gravar data e hora de vendas, para os filtros por dia não fecharem errado.
- [ ] Testes: o saldo bate depois de uma sequência de venda, perda, troca e ajuste; não há concorrência vendendo o mesmo produto; o isolamento entre negócios se mantém.

---

## Etapa 4: despesas

- [ ] **Despesa** (por negócio): data, valor, tipo (`GASOLINA`, `PEDAGIO`, `PNEU`, `MECANICA`, `OUTROS`, talvez uma tabela editável), descrição, veículo opcional e usuário que lançou.
- [ ] Listagem com filtros por período, tipo e veículo, e totais por tipo e por veículo.
- [ ] Testes, incluindo o isolamento.

> Depois desta etapa o frontend (etapa 8) já pode começar em paralelo, porque os cadastros, as vendas e as despesas estarão prontos.

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

## Etapa 6: relatórios PDF e API do dashboard

- [ ] **Cálculo de lucro/prejuízo** por período: vendas − CMV (custo das mercadorias vendidas, pelo custo guardado em cada item de venda) − despesas. Perdas aparecem à parte (custo das perdas).
- [ ] Períodos: dia, semana, quinzena, mês, trimestre, semestre e ano (e um intervalo livre).
- [ ] **PDF gerado no backend**, por exemplo com OpenPDF ou JasperReports, para o mobile reaproveitar: `GET /api/relatorio/lucro?periodo=MES&data=...`
- [ ] **Endpoints do dashboard**, em JSON para o Recharts:
  - vendas por dia;
  - compras;
  - despesas por tipo;
  - lucro por período;
  - produtos mais vendidos;
  - perdas.
- [ ] Testes com dados conhecidos, conferindo o valor do lucro.

---

## Etapa 7: pronto para produção

Itens de segurança e operação que estão pendentes desde a etapa 1. Devem ficar prontos **antes de colocar o sistema na internet**.

- [ ] **OpenAPI (springdoc)**: documentação em `/swagger-ui` e o JSON para gerar o cliente TypeScript do front e do mobile.
- [ ] **Limite de tentativas de login**: bloqueio após N falhas, por IP e por e-mail.
- [ ] **Senha**: endpoint para o usuário trocar a própria senha e para o ADMIN redefinir a de outro.
- [ ] **Desativar conta** (flag `ativo`) e invalidar o token de usuário desativado (checar no banco ou reduzir a expiração e usar refresh token).
- [ ] **Teste de arquitetura (ArchUnit)**: todo controller de dados de negócio recebe `NegocioAtual`.
- [ ] **Perfil de produção**: logs, `show-sql` desligado, CORS só para o domínio do front.
- [ ] **Empacotamento**: Dockerfile do backend e `compose` de produção (backend + Postgres, com volume).
- [ ] **Backup do banco** (pg_dump agendado) e teste de restauração.
- [ ] **Deploy**: onde hospedar (VPS barata, Railway, Render…), HTTPS (Caddy/nginx) e variáveis de ambiente.

**→ Com as etapas 1 a 7 concluídas, o MVP do backend está pronto.**

---

## Etapa 8: frontend (depois ou em paralelo, a partir da etapa 4)

- Vite + React + TypeScript + Tailwind + shadcn/ui + TanStack Query/Table + React Hook Form/Zod + Recharts.
- Cliente da API gerado a partir do OpenAPI.
- Login, seletor de negócio (envia `X-Negocio-Id`), cadastros, venda rápida, perda, troca, ajuste, despesas, compras, dashboard e download dos PDFs.

## Etapa 9: depois do MVP

- **WhatsApp**: avisos em tempo real de vendas, perdas e trocas, assinando os eventos da etapa 3.
- **App React Native**: reusa a API e o cliente gerado.
- **Módulo Pão Nino**: a estrutura multi-negócio já suporta; falta definir as regras próprias dele.

---

## Decisões em aberto

| Assunto | Quem decide | Afeta |
|---------|-------------|-------|
| Formato do pedido ao fornecedor | Mãe | Etapa 5 |
| Venda sem cliente é permitida? | Pais | Etapa 3 |
| O estoque pode ficar negativo? | Pais | Etapa 3 |
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
./mvnw spring-boot:run
```

- Precisa de JDK 21 **com `javac`** (por exemplo `openjdk-21-jdk`). No PC atual se usa `JAVA_HOME=~/.jdks/corretto-22.0.2`.
- As skills e os agents do Claude Code estão em `.claude/` e vêm junto no clone.
