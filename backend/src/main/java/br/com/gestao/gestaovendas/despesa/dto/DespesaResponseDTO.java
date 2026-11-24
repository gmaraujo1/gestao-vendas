package br.com.gestao.gestaovendas.despesa.dto;

import br.com.gestao.gestaovendas.despesa.domain.Despesa;
import br.com.gestao.gestaovendas.despesa.domain.TipoDespesa;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaResponseDTO(
        Long id,
        LocalDate data,
        BigDecimal valor,
        TipoDespesa tipo,
        String descricao,
        Long veiculoId,
        String veiculoDescricao,
        Long usuarioId
) {

    public static DespesaResponseDTO de(Despesa d) {
        var v = d.getVeiculo();
        return new DespesaResponseDTO(d.getId(), d.getData(), d.getValor(), d.getTipo(), d.getDescricao(),
                v == null ? null : v.getId(), v == null ? null : v.getDescricao(), d.getUsuarioId());
    }
}
