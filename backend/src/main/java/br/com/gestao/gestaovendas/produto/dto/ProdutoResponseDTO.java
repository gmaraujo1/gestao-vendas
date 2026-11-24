package br.com.gestao.gestaovendas.produto.dto;

import br.com.gestao.gestaovendas.common.enums.Categoria;
import br.com.gestao.gestaovendas.common.enums.UnidadeMedida;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

    private BigDecimal precoMedio;

}
