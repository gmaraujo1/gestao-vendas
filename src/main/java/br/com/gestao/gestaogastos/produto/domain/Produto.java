package br.com.gestao.gestaogastos.produto.domain;

import br.com.gestao.gestaogastos.common.enums.Categoria;
import br.com.gestao.gestaogastos.common.enums.UnidadeMedida;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 12)
    private Integer codigo;

    @Column(name = "descricao", nullable = false, length = 128)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 20)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade", nullable = false, length = 6)
    private UnidadeMedida unidadeMedida;

    @Column(name = "preco_medio", nullable = false)
    private Double precoMedio;

    @Column(name = "observacao", length = 100)
    private String observacao;

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false, precision = 3)
    private LocalDateTime criadoEm;

    @LastModifiedDate
    @Column(name = "atualizado_em", nullable = false, precision = 3)
    private LocalDateTime atualizadoEm;

}

