package br.com.gestao.gestaovendas.relatorio.domain;

import br.com.gestao.gestaovendas.relatorio.dto.ResultadoDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Soma vendas, custos, despesas e perdas de uma faixa e calcula o resultado. */
public final class Totais {

    private long numeroVendas;
    private BigDecimal vendas = BigDecimal.ZERO;
    private BigDecimal cmv = BigDecimal.ZERO;
    private BigDecimal despesas = BigDecimal.ZERO;
    private BigDecimal perdas = BigDecimal.ZERO;

    public void somarVendas(long numero, BigDecimal valor, BigDecimal custo) {
        numeroVendas += numero;
        vendas = vendas.add(valor);
        cmv = cmv.add(custo);
    }

    public void somarDespesas(BigDecimal valor) {
        despesas = despesas.add(valor);
    }

    public void somarPerdas(BigDecimal valor) {
        perdas = perdas.add(valor);
    }

    public void somar(Totais outro) {
        somarVendas(outro.numeroVendas, outro.vendas, outro.cmv);
        somarDespesas(outro.despesas);
        somarPerdas(outro.perdas);
    }

    public ResultadoDTO resultado() {
        BigDecimal lucroBruto = vendas.subtract(cmv);
        BigDecimal resultado = lucroBruto.subtract(despesas);
        return new ResultadoDTO(numeroVendas, dinheiro(vendas), dinheiro(cmv), dinheiro(lucroBruto),
                dinheiro(despesas), dinheiro(resultado), dinheiro(perdas), dinheiro(resultado.subtract(perdas)));
    }

    /** Os valores já chegam com 2 casas (arredondados por linha); só padroniza a escala. */
    private static BigDecimal dinheiro(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
