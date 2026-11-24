package br.com.gestao.gestaovendas.venda.domain;

import br.com.gestao.gestaovendas.cliente.domain.Cliente;
import br.com.gestao.gestaovendas.common.domain.EntidadeAuditavel;
import br.com.gestao.gestaovendas.exception.BusinessException;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Venda a um cliente. Não é editada nem apagada: erros se corrigem cancelando (estorno). */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "venda")
public class Venda extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "negocio_id", nullable = false, updatable = false)
    private Long negocioId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, updatable = false)
    private Cliente cliente;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Column(name = "total", nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusVenda status = StatusVenda.REGISTRADA;

    @Column(name = "observacao", length = 255)
    private String observacao;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    @Column(name = "cancelada_em")
    private LocalDateTime canceladaEm;

    @Column(name = "cancelada_por_id")
    private Long canceladaPorId;

    @Column(name = "motivo_cancelamento", length = 255)
    private String motivoCancelamento;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<VendaItem> itens = new ArrayList<>();

    public Venda(Long negocioId, Cliente cliente, LocalDateTime dataHora, String observacao, Long usuarioId) {
        this.negocioId = negocioId;
        this.cliente = cliente;
        this.dataHora = dataHora;
        this.observacao = observacao;
        this.usuarioId = usuarioId;
    }

    public void adicionarItem(VendaItem item) {
        item.vincular(this);
        itens.add(item);
        total = total.add(item.getSubtotal());
    }

    public void cancelar(String motivo, Long usuarioId, LocalDateTime quando) {
        if (status == StatusVenda.CANCELADA) {
            throw new BusinessException("A venda já está cancelada");
        }
        status = StatusVenda.CANCELADA;
        motivoCancelamento = motivo;
        canceladaPorId = usuarioId;
        canceladaEm = quando;
    }
}
