package br.com.gestao.gestaogastos.produto.dto;

import br.com.gestao.gestaogastos.common.enums.Categoria;
import br.com.gestao.gestaogastos.common.enums.UnidadeMedida;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoResponseDTO {

    private Long id;

    private Integer codigo;

    private String descricao;

    private Categoria categoria;

    private UnidadeMedida unidadeMedida;

    private Double precoMedio;

}
