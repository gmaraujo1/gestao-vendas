package br.com.gestao.gestaovendas.veiculo.domain;

import br.com.gestao.gestaovendas.common.domain.EntidadeAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Fiorino, caminhão... Global: os dois negócios usam os mesmos veículos.
 * A despesa (etapa 4) é que diz de qual negócio é o gasto.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "veiculo")
public class Veiculo extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descricao", nullable = false, length = 60)
    private String descricao;

    /** Opcional; guardada em maiúsculas. */
    @Column(name = "placa", length = 10)
    private String placa;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;
}
