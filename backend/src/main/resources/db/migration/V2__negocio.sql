-- Dois negócios com dados isolados. Cada usuário acessa os negócios vinculados a ele.

CREATE TABLE negocio (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo    VARCHAR(30)  NOT NULL,
    nome      VARCHAR(100) NOT NULL,
    ativo     BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP(3) NOT NULL DEFAULT now(),
    CONSTRAINT uk_negocio_codigo UNIQUE (codigo)
);

INSERT INTO negocio (codigo, nome) VALUES
    ('LATICINIO', 'Laticínio'),
    ('PAO_NINO',  'Pão Nino');

CREATE TABLE usuario_negocio (
    usuario_id BIGINT NOT NULL,
    negocio_id BIGINT NOT NULL,
    CONSTRAINT pk_usuario_negocio PRIMARY KEY (usuario_id, negocio_id),
    CONSTRAINT fk_usuario_negocio_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_negocio_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id)
);
CREATE INDEX ix_usuario_negocio_negocio ON usuario_negocio (negocio_id);

-- Usuários que já existirem: ADMIN acessa todos; FUNCIONARIO, o Laticínio.
INSERT INTO usuario_negocio (usuario_id, negocio_id)
SELECT u.id, n.id FROM usuario u CROSS JOIN negocio n WHERE u.tipo = 'ADMIN';
INSERT INTO usuario_negocio (usuario_id, negocio_id)
SELECT u.id, n.id FROM usuario u JOIN negocio n ON n.codigo = 'LATICINIO' WHERE u.tipo = 'FUNCIONARIO';

-- Produto passa a pertencer a um negócio; o código é único dentro do negócio.
ALTER TABLE produto ADD COLUMN negocio_id BIGINT;
UPDATE produto SET negocio_id = (SELECT id FROM negocio WHERE codigo = 'LATICINIO');
ALTER TABLE produto
    ALTER COLUMN negocio_id SET NOT NULL,
    ADD CONSTRAINT fk_produto_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id),
    DROP CONSTRAINT uk_produto_codigo,
    -- também serve de índice para os filtros por negócio
    ADD CONSTRAINT uk_produto_negocio_codigo UNIQUE (negocio_id, codigo);
