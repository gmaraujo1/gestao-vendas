package br.com.gestao.gestaovendas.estoque.dto;

import br.com.gestao.gestaovendas.common.dto.PaginaDTO;

import java.math.BigDecimal;

public record ExtratoProdutoDTO(
        Long produtoId,
        String descricao,
        BigDecimal saldo,
        PaginaDTO<MovimentacaoResponseDTO> movimentacoes
) {
}
