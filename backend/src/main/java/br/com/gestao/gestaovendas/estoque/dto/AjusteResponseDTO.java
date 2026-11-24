package br.com.gestao.gestaovendas.estoque.dto;

import java.math.BigDecimal;

/** movimentacaoId é null quando a contagem já batia com o saldo (nada a ajustar). */
public record AjusteResponseDTO(
        Long produtoId,
        BigDecimal saldoAnterior,
        BigDecimal saldoAtual,
        BigDecimal diferenca,
        Long movimentacaoId
) {
}
