package br.com.gestao.gestaogastos.produto.dto;

import java.time.LocalDate;

public record ProdutoFiltroDTO(
        String descricao,
        Double precoMinimo,
        Double precoMaximo,
        LocalDate atualizadoDe,
        LocalDate atualizadoAte,
        LocalDate criadoDe,
        LocalDate criadoAte
) {
}
