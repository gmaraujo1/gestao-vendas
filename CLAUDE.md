# Gestão Vendas

Sistema de gestão feito para os pais do autor. Os dados de cada negócio ficam isolados (dois sistemas em um):
- **Laticínio**: o foco agora.
- **Pão Nino**: também é fornecedor. O módulo dele fica para depois, mas a estrutura multi-negócio já deve suportá-lo.

Usuários: os pais (ADMIN) e um funcionário (FUNCIONARIO). Clientes **não** acessam o sistema.

## Estrutura

```
backend/    Java 21, Spring Boot 3.5, Spring Data JPA, PostgreSQL, MapStruct, Lombok (Maven)
frontend/   ainda vazio. Planejado: Vite + React + TS + Tailwind + shadcn/ui + TanStack Query/Table + RHF/Zod + Recharts
mobile/     React Native, só depois que o frontend e o backend estiverem prontos
.claude/    skills (todas as do ECC) e agents revisores (java, database, security, react, typescript)
```

## Comandos

```bash
cd backend && ./mvnw test   # testes (o CI roda o mesmo em PRs para main)
```
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

- Dinheiro sempre em `BigDecimal` (o `Produto.precoMedio` atual ainda está em `Double`; corrigir).
- Migrations com Flyway (o `ddl-auto: validate` atual exige tabelas criadas à mão).
- Login com Spring Security + JWT. OpenAPI (springdoc) para gerar o cliente TS do front e do mobile.
- PDF gerado no backend, para que o mobile reaproveite.
- Testcontainers para testes de integração com Postgres.
- Padrão atual por módulo: `domain/`, `application/` (um use case por ação), `controller/`, `repository/`, `dto/`, `mapper/`.
- Pacote base `br.com.gestao.gestaovendas`, artefato `gestao-vendas`, banco `gestao_vendas`.

## Roadmap

1. Base do backend: Flyway, entidade Negócio com isolamento, BigDecimal, JWT, ajuste de nomes.
2. Cadastros: produtos (adaptar o existente), clientes, veículos.
3. Estoque e vendas: movimentações, vendas, perdas, trocas, ajuste.
4. Despesas.
5. Frontend (Vite + React) nas telas dos passos 2 a 4.
6. Dashboard, relatórios PDF e compras (as compras dependem de confirmar o formato do pedido).

Cada etapa sai em uma branch e um PR, com testes. Revisar com o agent `java-reviewer` (ou `react-reviewer`/`typescript-reviewer` no front) antes do PR.
