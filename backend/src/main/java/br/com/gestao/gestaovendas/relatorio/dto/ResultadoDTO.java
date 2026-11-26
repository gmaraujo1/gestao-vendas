package br.com.gestao.gestaovendas.relatorio.dto;

import java.math.BigDecimal;

/**
 * Lucro ou prejuízo de um intervalo.
 *
 * @param numeroVendas       vendas registradas (as canceladas não entram)
 * @param vendas             valor vendido
 * @param cmv                custo das mercadorias vendidas (custo médio guardado em cada item)
 * @param lucroBruto         vendas − cmv
 * @param despesas           gasolina, pedágio, manutenção...
 * @param resultado          lucroBruto − despesas: positivo é lucro, negativo é prejuízo
 * @param perdas             custo dos produtos perdidos (vencidos, furados, devolvidos em troca)
 * @param resultadoComPerdas resultado − perdas
 */
public record ResultadoDTO(
        long numeroVendas,
        BigDecimal vendas,
        BigDecimal cmv,
        BigDecimal lucroBruto,
        BigDecimal despesas,
        BigDecimal resultado,
        BigDecimal perdas,
        BigDecimal resultadoComPerdas
) {
}
