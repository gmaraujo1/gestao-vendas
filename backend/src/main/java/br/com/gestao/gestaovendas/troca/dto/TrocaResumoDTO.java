package br.com.gestao.gestaovendas.troca.dto;

import br.com.gestao.gestaovendas.troca.domain.Troca;

import java.time.LocalDateTime;

/** Linha da listagem de trocas (os itens vêm no detalhe, GET /api/troca/{id}). */
public record TrocaResumoDTO(
        Long id,
        LocalDateTime dataHora,
        Long clienteId,
        String clienteNome,
        String observacao
) {

    public static TrocaResumoDTO de(Troca t) {
        return new TrocaResumoDTO(t.getId(), t.getDataHora(), t.getCliente().getId(), t.getCliente().getNome(),
                t.getObservacao());
    }
}
