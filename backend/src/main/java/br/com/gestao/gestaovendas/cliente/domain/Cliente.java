package br.com.gestao.gestaovendas.cliente.domain;

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

/** Quem compra. Só cadastro: o cliente não acessa o sistema. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cliente")
public class Cliente extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "negocio_id", nullable = false, updatable = false)
    private Long negocioId;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    /** Também serve para os avisos por WhatsApp no futuro. */
    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "endereco", length = 150)
    private String endereco;

    @Column(name = "cidade", length = 80)
    private String cidade;

    @Column(name = "observacao", length = 255)
    private String observacao;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;
}
