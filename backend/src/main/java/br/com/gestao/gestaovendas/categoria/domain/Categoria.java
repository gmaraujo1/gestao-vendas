package br.com.gestao.gestaovendas.categoria.domain;

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

/** Categoria de produto, própria de cada negócio. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "categoria")
public class Categoria extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "negocio_id", nullable = false, updatable = false)
    private Long negocioId;

    @Column(name = "nome", nullable = false, length = 60)
    private String nome;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;
}
