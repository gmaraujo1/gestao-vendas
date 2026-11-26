package br.com.gestao.gestaovendas.relatorio.application;

import br.com.gestao.gestaovendas.common.dto.Periodo;
import br.com.gestao.gestaovendas.relatorio.domain.IntervaloRelatorio;
import br.com.gestao.gestaovendas.relatorio.domain.TipoPeriodo;
import br.com.gestao.gestaovendas.relatorio.domain.Totais;
import br.com.gestao.gestaovendas.relatorio.dto.PontoSerieDTO;
import br.com.gestao.gestaovendas.relatorio.dto.SerieDTO;
import br.com.gestao.gestaovendas.relatorio.repository.RelatorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Calcula o resultado por faixa (dia, semana, mês...). O banco devolve os totais por dia e
 * aqui eles são somados nas faixas, de modo que o total do período é sempre a soma dos pontos.
 * Faixas sem movimento aparecem zeradas (o gráfico não pula dias).
 */
@Component
@RequiredArgsConstructor
class CalculadoraResultado {

    private final RelatorioRepository repository;

    SerieDTO serie(Long negocioId, IntervaloRelatorio intervalo, TipoPeriodo agrupamento) {
        TreeMap<LocalDate, Totais> porInicio = new TreeMap<>();
        List<IntervaloRelatorio.Faixa> faixas = intervalo.faixas(agrupamento);
        faixas.forEach(f -> porInicio.put(f.inicio(), new Totais()));

        Periodo periodo = intervalo.periodo();
        repository.vendasPorDia(negocioId, periodo)
                .forEach(d -> faixa(porInicio, d.dia()).somarVendas(d.numeroVendas(), d.vendas(), d.cmv()));
        repository.despesasPorDia(negocioId, periodo)
                .forEach(d -> faixa(porInicio, d.dia()).somarDespesas(d.valor()));
        repository.perdasPorDia(negocioId, periodo)
                .forEach(d -> faixa(porInicio, d.dia()).somarPerdas(d.valor()));

        Totais total = new Totais();
        List<PontoSerieDTO> pontos = new ArrayList<>(faixas.size());
        for (IntervaloRelatorio.Faixa f : faixas) {
            Totais t = porInicio.get(f.inicio());
            total.somar(t);
            pontos.add(PontoSerieDTO.de(f.inicio(), f.fim(), t.resultado()));
        }
        return new SerieDTO(intervalo.de(), intervalo.ate(), agrupamento, pontos, total.resultado());
    }

    private static Totais faixa(TreeMap<LocalDate, Totais> porInicio, LocalDate dia) {
        Map.Entry<LocalDate, Totais> entrada = porInicio.floorEntry(dia);
        if (entrada == null) {
            throw new IllegalStateException("Dia fora do intervalo do relatório: " + dia);
        }
        return entrada.getValue();
    }
}
