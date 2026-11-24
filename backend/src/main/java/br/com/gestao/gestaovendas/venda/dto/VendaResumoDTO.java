package br.com.gestao.gestaovendas.venda.dto;

import br.com.gestao.gestaovendas.venda.domain.StatusVenda;
import br.com.gestao.gestaovendas.venda.domain.Venda;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Linha da listagem de vendas. */
public record VendaResumoDTO(
        Long id,
        LocalDateTime dataHora,
        Long clienteId,
        String clienteNome,
        BigDecimal total,
        StatusVenda status
) {

    public static VendaResumoDTO de(Venda v) {
        return new VendaResumoDTO(v.getId(), v.getDataHora(), v.getCliente().getId(), v.getCliente().getNome(),
                v.getTotal(), v.getStatus());
    }
}
