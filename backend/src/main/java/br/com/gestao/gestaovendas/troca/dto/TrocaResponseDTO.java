package br.com.gestao.gestaovendas.troca.dto;

import br.com.gestao.gestaovendas.estoque.dto.MovimentacaoResponseDTO;
import br.com.gestao.gestaovendas.troca.domain.Troca;

import java.time.LocalDateTime;
import java.util.List;

/** Detalhe da troca, com as movimentações TROCA_ENTRADA/PERDA/TROCA_SAIDA. */
public record TrocaResponseDTO(
        Long id,
        LocalDateTime dataHora,
        Long clienteId,
        String clienteNome,
        String observacao,
        Long usuarioId,
        List<MovimentacaoResponseDTO> movimentacoes
) {

    public static TrocaResponseDTO de(Troca t, List<MovimentacaoResponseDTO> movimentacoes) {
        return new TrocaResponseDTO(t.getId(), t.getDataHora(), t.getCliente().getId(), t.getCliente().getNome(),
                t.getObservacao(), t.getUsuarioId(), movimentacoes);
    }
}
