-- Etapas 3 e 4: movimentações de estoque, vendas, trocas e despesas.
-- O saldo de um produto é SEMPRE a soma das movimentações dele (nunca editado direto).

-- Alvos das FKs compostas: garantem no banco que tudo aponta para o mesmo negócio.
ALTER TABLE produto ADD CONSTRAINT uk_produto_id_negocio UNIQUE (id, negocio_id);
ALTER TABLE cliente ADD CONSTRAINT uk_cliente_id_negocio UNIQUE (id, negocio_id);

-- Venda: cliente obrigatório. Não é editada nem apagada; é cancelada (estorno no estoque).
CREATE TABLE venda (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    negocio_id          BIGINT        NOT NULL,
    cliente_id          BIGINT        NOT NULL,
    data_hora           TIMESTAMP(3)  NOT NULL,
    total               NUMERIC(14,2) NOT NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'REGISTRADA',
    observacao          VARCHAR(255),
    usuario_id          BIGINT        NOT NULL,
    cancelada_em        TIMESTAMP(3),
    cancelada_por_id    BIGINT,
    motivo_cancelamento VARCHAR(255),
    criado_em           TIMESTAMP(3)  NOT NULL DEFAULT now(),
    atualizado_em       TIMESTAMP(3)  NOT NULL DEFAULT now(),
    CONSTRAINT fk_venda_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id),
    CONSTRAINT fk_venda_cliente FOREIGN KEY (cliente_id, negocio_id) REFERENCES cliente (id, negocio_id),
    CONSTRAINT fk_venda_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_venda_cancelada_por FOREIGN KEY (cancelada_por_id) REFERENCES usuario (id),
    CONSTRAINT uk_venda_id_negocio UNIQUE (id, negocio_id),
    CONSTRAINT ck_venda_status CHECK (status IN ('REGISTRADA', 'CANCELADA')),
    CONSTRAINT ck_venda_total CHECK (total >= 0)
);
CREATE INDEX ix_venda_negocio_data ON venda (negocio_id, data_hora);
CREATE INDEX ix_venda_cliente ON venda (cliente_id, data_hora);

CREATE TABLE venda_item (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    venda_id       BIGINT        NOT NULL,
    negocio_id     BIGINT        NOT NULL,
    produto_id     BIGINT        NOT NULL,
    quantidade     NUMERIC(14,3) NOT NULL,
    preco_unitario NUMERIC(14,2) NOT NULL,
    -- custo médio do produto no momento da venda: base do lucro (etapa 6)
    custo_unitario NUMERIC(14,4) NOT NULL,
    subtotal       NUMERIC(14,2) NOT NULL,
    CONSTRAINT fk_venda_item_venda FOREIGN KEY (venda_id, negocio_id) REFERENCES venda (id, negocio_id) ON DELETE CASCADE,
    CONSTRAINT fk_venda_item_produto FOREIGN KEY (produto_id, negocio_id) REFERENCES produto (id, negocio_id),
    CONSTRAINT ck_venda_item_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_venda_item_preco CHECK (preco_unitario >= 0)
);
CREATE INDEX ix_venda_item_venda ON venda_item (venda_id);
CREATE INDEX ix_venda_item_produto ON venda_item (produto_id);

-- Troca: cabeçalho; os itens devolvidos/entregues ficam nas movimentações (origem TROCA).
CREATE TABLE troca (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    negocio_id    BIGINT       NOT NULL,
    cliente_id    BIGINT       NOT NULL,
    data_hora     TIMESTAMP(3) NOT NULL,
    observacao    VARCHAR(255),
    usuario_id    BIGINT       NOT NULL,
    criado_em     TIMESTAMP(3) NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP(3) NOT NULL DEFAULT now(),
    CONSTRAINT fk_troca_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id),
    CONSTRAINT fk_troca_cliente FOREIGN KEY (cliente_id, negocio_id) REFERENCES cliente (id, negocio_id),
    CONSTRAINT fk_troca_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT uk_troca_id_negocio UNIQUE (id, negocio_id)
);
CREATE INDEX ix_troca_negocio_data ON troca (negocio_id, data_hora);

-- Movimentação: cada entrada (+) ou saída (-) de estoque.
CREATE TABLE movimentacao_estoque (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    negocio_id     BIGINT        NOT NULL,
    produto_id     BIGINT        NOT NULL,
    tipo           VARCHAR(20)   NOT NULL,
    -- com sinal: positiva entra, negativa sai
    quantidade     NUMERIC(14,3) NOT NULL,
    custo_unitario NUMERIC(14,4) NOT NULL,
    data_hora      TIMESTAMP(3)  NOT NULL,
    motivo_perda   VARCHAR(20),
    observacao     VARCHAR(255),
    venda_id       BIGINT,
    troca_id       BIGINT,
    usuario_id     BIGINT        NOT NULL,
    criado_em      TIMESTAMP(3)  NOT NULL DEFAULT now(),
    CONSTRAINT fk_mov_produto FOREIGN KEY (produto_id, negocio_id) REFERENCES produto (id, negocio_id),
    -- compostas: com venda_id/troca_id nulo a FK não é checada (MATCH SIMPLE)
    CONSTRAINT fk_mov_venda FOREIGN KEY (venda_id, negocio_id) REFERENCES venda (id, negocio_id),
    CONSTRAINT fk_mov_troca FOREIGN KEY (troca_id, negocio_id) REFERENCES troca (id, negocio_id),
    CONSTRAINT fk_mov_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_mov_tipo CHECK (tipo IN
        ('VENDA', 'VENDA_ESTORNO', 'PERDA', 'TROCA_ENTRADA', 'TROCA_SAIDA', 'AJUSTE', 'COMPRA')),
    CONSTRAINT ck_mov_quantidade CHECK (quantidade <> 0),
    CONSTRAINT ck_mov_motivo CHECK (
        (tipo = 'PERDA' AND motivo_perda IN ('FURADO', 'VENCIDO', 'AVARIADO', 'OUTRO'))
        OR (tipo <> 'PERDA' AND motivo_perda IS NULL))
);
-- saldo e extrato por produto
CREATE INDEX ix_mov_negocio_produto_data ON movimentacao_estoque (negocio_id, produto_id, data_hora);
-- relatórios por período/tipo (perdas, vendas)
CREATE INDEX ix_mov_negocio_tipo_data ON movimentacao_estoque (negocio_id, tipo, data_hora);
CREATE INDEX ix_mov_venda ON movimentacao_estoque (venda_id) WHERE venda_id IS NOT NULL;
CREATE INDEX ix_mov_troca ON movimentacao_estoque (troca_id) WHERE troca_id IS NOT NULL;

-- Despesa (etapa 4): por negócio; o veículo é opcional e global.
CREATE TABLE despesa (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    negocio_id    BIGINT        NOT NULL,
    data          DATE          NOT NULL,
    valor         NUMERIC(14,2) NOT NULL,
    tipo          VARCHAR(20)   NOT NULL,
    descricao     VARCHAR(255),
    veiculo_id    BIGINT,
    usuario_id    BIGINT        NOT NULL,
    criado_em     TIMESTAMP(3)  NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP(3)  NOT NULL DEFAULT now(),
    CONSTRAINT fk_despesa_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id),
    CONSTRAINT fk_despesa_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculo (id),
    CONSTRAINT fk_despesa_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_despesa_valor CHECK (valor > 0),
    CONSTRAINT ck_despesa_tipo CHECK (tipo IN ('GASOLINA', 'PEDAGIO', 'PNEU', 'MECANICA', 'OUTROS'))
);
CREATE INDEX ix_despesa_negocio_data ON despesa (negocio_id, data);
CREATE INDEX ix_despesa_veiculo ON despesa (veiculo_id) WHERE veiculo_id IS NOT NULL;
