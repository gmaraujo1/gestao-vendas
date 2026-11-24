package br.com.gestao.gestaovendas.troca.domain;

import br.com.gestao.gestaovendas.cliente.domain.Cliente;
import br.com.gestao.gestaovendas.common.domain.EntidadeAuditavel;
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

import java.time.LocalDateTime;

/**
 * Cliente devolve produtos e leva outros. Os itens ficam nas movimentações (troca_id):
 * TROCA_ENTRADA (devolvido que volta ao estoque), PERDA (devolvido descartado) e TROCA_SAIDA (entregue).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "troca")
public class Troca extends EntidadeAuditavel {

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

    @Column(name = "observacao", length = 255)
    private String observacao;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    public Troca(Long negocioId, Cliente cliente, LocalDateTime dataHora, String observacao, Long usuarioId) {
        this.negocioId = negocioId;
        this.cliente = cliente;
        this.dataHora = dataHora;
        this.observacao = observacao;
        this.usuarioId = usuarioId;
    }
}
