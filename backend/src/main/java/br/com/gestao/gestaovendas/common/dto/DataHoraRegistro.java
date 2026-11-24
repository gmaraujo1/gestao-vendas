package br.com.gestao.gestaovendas.common.dto;

import br.com.gestao.gestaovendas.exception.RequisicaoInvalidaException;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Data/hora de um registro (venda, perda...). Sem valor = agora. Pode ser no passado
 * (lançar as vendas de ontem), mas não no futuro.
 */
public final class DataHoraRegistro {

    /** Folga para relógios de celular levemente adiantados. */
    private static final Duration TOLERANCIA = Duration.ofMinutes(5);

    private DataHoraRegistro() {
    }

    public static LocalDateTime resolver(LocalDateTime informada, Clock clock) {
        LocalDateTime agora = LocalDateTime.now(clock);
        if (informada == null) {
            return agora;
        }
        if (informada.isAfter(agora.plus(TOLERANCIA))) {
            throw new RequisicaoInvalidaException("A data/hora não pode ser no futuro");
        }
        return informada;
    }
}
