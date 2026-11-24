package br.com.gestao.gestaovendas.venda.dto;

import br.com.gestao.gestaovendas.venda.domain.StatusVenda;
import br.com.gestao.gestaovendas.venda.domain.Venda;
import br.com.gestao.gestaovendas.venda.domain.VendaItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VendaResponseDTO(
        Long id,
        LocalDateTime dataHora,
        Long clienteId,
        String clienteNome,
        BigDecimal total,
        StatusVenda status,
        String observacao,
        Long usuarioId,
        LocalDateTime canceladaEm,
        String motivoCancelamento,
        List<Item> itens
) {

    public record Item(Long produtoId, Integer produtoCodigo, String produtoDescricao,
                       BigDecimal quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {

        static Item de(VendaItem i) {
            return new Item(i.getProduto().getId(), i.getProduto().getCodigo(), i.getProduto().getDescricao(),
                    i.getQuantidade(), i.getPrecoUnitario(), i.getSubtotal());
        }
    }

    public static VendaResponseDTO de(Venda v) {
        return new VendaResponseDTO(v.getId(), v.getDataHora(), v.getCliente().getId(), v.getCliente().getNome(),
                v.getTotal(), v.getStatus(), v.getObservacao(), v.getUsuarioId(), v.getCanceladaEm(),
                v.getMotivoCancelamento(), v.getItens().stream().map(Item::de).toList());
    }
}
