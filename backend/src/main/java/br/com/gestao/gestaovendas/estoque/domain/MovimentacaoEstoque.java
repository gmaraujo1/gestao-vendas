package br.com.gestao.gestaovendas.estoque.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Uma entrada (+) ou saída (-) de estoque. Imutável: erros se corrigem com outra
 * movimentação (estorno ou ajuste), nunca editando ou apagando esta.
 * O saldo de um produto é a soma destas linhas.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "movimentacao_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "negocio_id", nullable = false, updatable = false)
    private Long negocioId;

    @Column(name = "produto_id", nullable = false, updatable = false)
    private Long produtoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, updatable = false, length = 20)
    private TipoMovimentacao tipo;

    /** Com sinal. */
    @Column(name = "quantidade", nullable = false, updatable = false, precision = 14, scale = 3)
    private BigDecimal quantidade;

    /** Custo médio do produto no momento: valoriza perdas e o custo das vendas. */
    @Column(name = "custo_unitario", nullable = false, updatable = false, precision = 14, scale = 4)
    private BigDecimal custoUnitario;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_perda", updatable = false, length = 20)
    private MotivoPerda motivoPerda;

    @Column(name = "observacao", updatable = false, length = 255)
    private String observacao;

    @Column(name = "venda_id", updatable = false)
    private Long vendaId;

    @Column(name = "troca_id", updatable = false)
    private Long trocaId;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
    }
}
