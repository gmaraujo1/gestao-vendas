package br.com.gestao.gestaovendas.estoque.domain;

import br.com.gestao.gestaovendas.common.evento.EventoDominio;

import java.math.BigDecimal;

/** Eventos de domínio de estoque, vendas e trocas. */
public final class EventosEstoque {

    private EventosEstoque() {
    }

    public record VendaRegistrada(Long negocioId, Long vendaId, Long clienteId, BigDecimal total, Long usuarioId)
            implements EventoDominio {
    }

    public record VendaCancelada(Long negocioId, Long vendaId, String motivo, Long usuarioId)
            implements EventoDominio {
    }

    public record PerdaRegistrada(Long negocioId, Long produtoId, BigDecimal quantidade, MotivoPerda motivo,
                                  Long usuarioId) implements EventoDominio {
    }

    public record TrocaRegistrada(Long negocioId, Long trocaId, Long clienteId, Long usuarioId)
            implements EventoDominio {
    }

    public record EstoqueAjustado(Long negocioId, Long produtoId, BigDecimal diferenca, Long usuarioId)
            implements EventoDominio {
    }
}
