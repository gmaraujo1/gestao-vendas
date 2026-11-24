package br.com.gestao.gestaovendas.produto.dto;

import br.com.gestao.gestaovendas.common.enums.Categoria;
import br.com.gestao.gestaovendas.common.enums.UnidadeMedida;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoRequestEditDTO {

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 3, max = 100, message = "Descrição deve ter entre 3 e 100 caracteres")
    private String descricao;

    @NotNull(message = "Categoria é obrigatória")
    private Categoria categoria;

    @NotNull(message = "Unidade de medida é obrigatória")
    private UnidadeMedida unidadeMedida;

    @NotNull(message = "Valor unitário é obrigatório")
    @Positive(message = "Valor unitário deve ser maior que R$ 0,00")
    @Digits(integer = 10, fraction = 4, message = "Valor unitário inválido")
    private BigDecimal precoMedio;

    private String observacao;

}
