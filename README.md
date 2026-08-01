# Gestão Vendas

Sistema desenvolvido para gerenciamento de **compras, vendas e controle de produtos**, com foco em organização, rastreabilidade e otimização dos processos internos de pequenas empresas.

O projeto tem como objetivo substituir controles manuais, como planilhas e anotações em papel, por uma solução centralizada capaz de registrar produtos, acompanhar movimentações, gerar relatórios e fornecer uma visão estratégica do negócio.

---

## Objetivo do Projeto

O **Gestão Vendas** foi criado para facilitar o controle de estoque e operações comerciais, permitindo que empresas acompanhem:

* Cadastro e gerenciamento de produtos;
* Registro de compras;
* Registro de vendas;
* Controle de quantidade disponível;
* Histórico de movimentações;
* Análise de gastos e faturamento;
* Visualização de indicadores do negócio.

---

## Funcionalidades

### Produtos

* Cadastro de produtos;
* Controle de código e descrição;
* Registro de valores;
* Gerenciamento de informações dos itens.

### Compras

* Registro de compras realizadas;
* Associação de produtos e quantidades;
* Cálculo automático do valor total;
* Histórico de aquisições;
* Exportação de informações.

### Vendas

* Registro de produtos vendidos;
* Controle de movimentações;
* Atualização de estoque;
* Histórico de vendas.

### Dashboard

* Indicadores de desempenho;
* Visualização de gastos;
* Acompanhamento de vendas;
* Análise geral do negócio.

---

## Tecnologias Utilizadas

### Backend

* Java 21
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Maven

### Frontend

* Next.js
* React
* TypeScript
* Tailwind CSS

### Bibliotecas e Ferramentas

* TanStack Table
* TanStack React Query
* Zustand
* Recharts
* React PDF

### Integrações

* n8n
* WhatsApp API

---

## Arquitetura do Projeto

O sistema segue uma arquitetura baseada na separação de responsabilidades:

```
gestao-vendas

├── backend
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── dto
│   └── exception
│
└── frontend
    ├── components
    ├── pages
    ├── hooks
    ├── services
    └── utils
```

---

## Modelo de Dados

Principais entidades do sistema:

### Usuário

Responsável pelo acesso ao sistema.

Principais atributos:

* Nome
* Email
* Senha
* Tipo de usuário

### Produto

Representa os itens comercializados.

Principais atributos:

* Código
* Descrição
* Unidade
* Valor médio

### Compra

Representa entradas de produtos.

Principais atributos:

* Data da compra
* Valor total
* Observações

### Compra Item

Relaciona produtos e compras.

Principais atributos:

* Produto
* Quantidade
* Valor unitário
* Subtotal

---

## Roadmap

### Versão Inicial (MVP)

* [x] Documentação do projeto
* [ ] Modelagem do banco de dados
* [ ] Protótipo das telas
* [ ] Cadastro de produtos
* [ ] Cadastro de compras
* [ ] Controle de estoque
* [ ] Dashboard inicial

### Futuras Implementações

* Integração completa com WhatsApp;
* Registro automático de vendas via mensagens;
* Relatórios avançados;
* Controle financeiro;
* Gestão de despesas;
* Controle de perdas e logística;
* Sistema de permissões.

---

## Instalação

### Pré-requisitos

* Java 21+
* Node.js 20+
* PostgreSQL
* Maven

---

### Backend

Clone o projeto:

```bash
git clone https://github.com/seu-usuario/gestao-vendas.git
```

Acesse o diretório:

```bash
cd backend
```

Execute:

```bash
mvn spring-boot:run
```

---

### Frontend

Acesse o diretório:

```bash
cd frontend
```

Instale as dependências:

```bash
npm install
```

Execute o projeto:

```bash
npm run dev
```

---

## Contribuição

Este projeto está em desenvolvimento e novas funcionalidades serão adicionadas conforme a evolução das necessidades do sistema.

Sugestões e melhorias são bem-vindas.

---

## Autor

Desenvolvido por **Gustavo M. A.**

Projeto criado para estudo, evolução técnica e aplicação prática de conceitos de desenvolvimento de software.
