package br.com.gestao.gestaovendas.relatorio.dto;

import br.com.gestao.gestaovendas.estoque.domain.MotivoPerda;

import java.math.BigDecimal;

/** @param registros quantas perdas foram lançadas com o motivo */
public record PerdaPorMotivoDTO(MotivoPerda motivo, long registros, BigDecimal custo) {
}
