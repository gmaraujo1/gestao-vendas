package br.com.gestao.gestaovendas.estoque.dto;

import br.com.gestao.gestaovendas.estoque.domain.MotivoPerda;
import br.com.gestao.gestaovendas.estoque.domain.MovimentacaoEstoque;
import br.com.gestao.gestaovendas.estoque.domain.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoResponseDTO(
        Long id,
        Long produtoId,
        /* Para a tela não precisar cruzar o id com a lista de produtos. */
        String produtoDescricao,
        TipoMovimentacao tipo,
        BigDecimal quantidade,
        BigDecimal custoUnitario,
        LocalDateTime dataHora,
        MotivoPerda motivoPerda,
        String observacao,
        Long vendaId,
        Long trocaId,
        Long usuarioId
) {

    public static MovimentacaoResponseDTO de(MovimentacaoEstoque m, String produtoDescricao) {
        return new MovimentacaoResponseDTO(m.getId(), m.getProdutoId(), produtoDescricao, m.getTipo(), m.getQuantidade(),
                m.getCustoUnitario(), m.getDataHora(), m.getMotivoPerda(), m.getObservacao(),
                m.getVendaId(), m.getTrocaId(), m.getUsuarioId());
    }
}
