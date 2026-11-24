package br.com.gestao.gestaovendas.despesa.domain;

import br.com.gestao.gestaovendas.common.domain.EntidadeAuditavel;
import br.com.gestao.gestaovendas.veiculo.domain.Veiculo;
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
import java.time.LocalDate;

/** Gasto do negócio (gasolina, pedágio...). Entra no lucro/prejuízo (etapa 6). */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "despesa")
public class Despesa extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "negocio_id", nullable = false, updatable = false)
    private Long negocioId;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "valor", nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoDespesa tipo;

    @Column(name = "descricao", length = 255)
    private String descricao;

    /** Opcional. Veículos são globais; a despesa diz de qual negócio é o gasto. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;
}
