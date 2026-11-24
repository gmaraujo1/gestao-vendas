package br.com.gestao.gestaovendas.produto.domain;

import br.com.gestao.gestaovendas.categoria.domain.Categoria;
import br.com.gestao.gestaovendas.common.domain.EntidadeAuditavel;
import br.com.gestao.gestaovendas.common.enums.UnidadeMedida;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "produto")
public class Produto extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "negocio_id", nullable = false, updatable = false)
    private Long negocioId;

    /** Único dentro do negócio. */
    @Column(name = "codigo", nullable = false)
    private Integer codigo;

    @Column(name = "descricao", nullable = false, length = 128)
    private String descricao;

    /** Sempre do mesmo negócio do produto (validado no use case). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade", nullable = false, length = 6)
    private UnidadeMedida unidadeMedida;

    @Column(name = "preco_medio", nullable = false, precision = 14, scale = 4)
    private BigDecimal precoMedio;

    @Column(name = "observacao", length = 100)
    private String observacao;

    /** Inativo some das telas de venda, mas mantém o histórico. */
    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;
}
