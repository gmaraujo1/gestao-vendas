-- Esquema inicial: usuários e produtos (equivalente às entidades existentes).

CREATE TABLE usuario (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    senha         VARCHAR(255) NOT NULL,
    tipo          VARCHAR(20)  NOT NULL,
    criado_em     TIMESTAMP(3) NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP(3) NOT NULL DEFAULT now(),
    CONSTRAINT ck_usuario_tipo CHECK (tipo IN ('ADMIN', 'FUNCIONARIO'))
);
-- A aplicação já grava o e-mail em minúsculas; o índice garante isso no banco também.
CREATE UNIQUE INDEX uk_usuario_email ON usuario (lower(email));

CREATE TABLE produto (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo        INTEGER       NOT NULL,
    descricao     VARCHAR(128)  NOT NULL,
    categoria     VARCHAR(20)   NOT NULL,
    unidade       VARCHAR(6)    NOT NULL,
    -- custo médio: 4 casas para não perder precisão no recálculo
    preco_medio   NUMERIC(14,4) NOT NULL,
    observacao    VARCHAR(100),
    criado_em     TIMESTAMP(3)  NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP(3)  NOT NULL DEFAULT now(),
    CONSTRAINT uk_produto_codigo UNIQUE (codigo),
    CONSTRAINT ck_produto_preco_medio CHECK (preco_medio >= 0)
);
