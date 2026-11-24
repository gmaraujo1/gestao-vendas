-- Etapa 2: categorias por negócio, clientes, veículos e produto ligado à categoria.

-- Categoria de produto (cada negócio tem as suas)
CREATE TABLE categoria (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    negocio_id    BIGINT       NOT NULL,
    nome          VARCHAR(60)  NOT NULL,
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMP(3) NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP(3) NOT NULL DEFAULT now(),
    CONSTRAINT fk_categoria_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id),
    -- alvo da FK composta de produto: garante categoria do mesmo negócio no banco
    CONSTRAINT uk_categoria_id_negocio UNIQUE (id, negocio_id)
);
-- nome único no negócio, sem diferenciar maiúsculas
CREATE UNIQUE INDEX uk_categoria_negocio_nome ON categoria (negocio_id, lower(nome));

-- Sugestão inicial para o Laticínio (editável pela API)
INSERT INTO categoria (negocio_id, nome)
SELECT n.id, c.nome
FROM negocio n
CROSS JOIN (VALUES ('Leites'), ('Queijos'), ('Iogurtes'), ('Bebidas lácteas'), ('Doces'), ('Outros')) AS c(nome)
WHERE n.codigo = 'LATICINIO';

-- Produto: o enum "categoria" vira referência à tabela.
-- Produtos já existentes ganham uma categoria com o nome do enum antigo, no próprio negócio.
INSERT INTO categoria (negocio_id, nome)
SELECT DISTINCT p.negocio_id, initcap(lower(p.categoria))
FROM produto p
WHERE NOT EXISTS (
    SELECT 1 FROM categoria c
    WHERE c.negocio_id = p.negocio_id AND lower(c.nome) = lower(p.categoria)
);

ALTER TABLE produto ADD COLUMN categoria_id BIGINT;
UPDATE produto p
SET categoria_id = c.id
FROM categoria c
WHERE c.negocio_id = p.negocio_id AND lower(c.nome) = lower(p.categoria);

ALTER TABLE produto
    ALTER COLUMN categoria_id SET NOT NULL,
    ADD CONSTRAINT fk_produto_categoria FOREIGN KEY (categoria_id, negocio_id)
        REFERENCES categoria (id, negocio_id),
    DROP COLUMN categoria,
    ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
CREATE INDEX ix_produto_categoria ON produto (categoria_id);

-- Cliente (só cadastro; não acessa o sistema)
CREATE TABLE cliente (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    negocio_id    BIGINT       NOT NULL,
    nome          VARCHAR(100) NOT NULL,
    telefone      VARCHAR(20),
    endereco      VARCHAR(150),
    cidade        VARCHAR(80),
    observacao    VARCHAR(255),
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMP(3) NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP(3) NOT NULL DEFAULT now(),
    CONSTRAINT fk_cliente_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id)
);
CREATE INDEX ix_cliente_negocio_nome ON cliente (negocio_id, nome);

-- Veículo: global (os dois negócios usam os mesmos). A despesa é que escolhe o negócio.
CREATE TABLE veiculo (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    descricao     VARCHAR(60)  NOT NULL,
    placa         VARCHAR(10),
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMP(3) NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP(3) NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX uk_veiculo_placa ON veiculo (upper(placa)) WHERE placa IS NOT NULL;

INSERT INTO veiculo (descricao) VALUES ('Fiorino'), ('Caminhão');
