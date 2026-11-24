package br.com.gestao.gestaovendas.venda.domain;

import br.com.gestao.gestaovendas.produto.domain.Produto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "venda_item")
public class VendaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_id", nullable = false, updatable = false)
    private Venda venda;

    @Column(name = "negocio_id", nullable = false, updatable = false)
    private Long negocioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false, updatable = false)
    private Produto produto;

    @Column(name = "quantidade", nullable = false, updatable = false, precision = 14, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "preco_unitario", nullable = false, updatable = false, precision = 14, scale = 2)
    private BigDecimal precoUnitario;

    /** Custo médio do produto na hora da venda: base do custo das mercadorias vendidas. */
    @Column(name = "custo_unitario", nullable = false, updatable = false, precision = 14, scale = 4)
    private BigDecimal custoUnitario;

    @Column(name = "subtotal", nullable = false, updatable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    public VendaItem(Produto produto, BigDecimal quantidade, BigDecimal precoUnitario) {
        this.produto = produto;
        this.negocioId = produto.getNegocioId();
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.custoUnitario = produto.getPrecoMedio();
        this.subtotal = quantidade.multiply(precoUnitario).setScale(2, RoundingMode.HALF_UP);
    }

    void vincular(Venda venda) {
        this.venda = venda;
    }
}
