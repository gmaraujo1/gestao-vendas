package br.com.gestao.gestaovendas.relatorio.application;

import br.com.gestao.gestaovendas.common.dto.Periodo;
import br.com.gestao.gestaovendas.despesa.repository.DespesaRepository;
import br.com.gestao.gestaovendas.relatorio.domain.CriterioRanking;
import br.com.gestao.gestaovendas.relatorio.domain.DescricaoPeriodo;
import br.com.gestao.gestaovendas.relatorio.domain.IntervaloRelatorio;
import br.com.gestao.gestaovendas.relatorio.dto.FiltroPeriodoDTO;
import br.com.gestao.gestaovendas.relatorio.dto.RelatorioLucroDTO;
import br.com.gestao.gestaovendas.relatorio.dto.SerieDTO;
import br.com.gestao.gestaovendas.relatorio.repository.RelatorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/** Lucro e prejuízo do período: vendas − custo das mercadorias vendidas − despesas, com as perdas à parte. */
@Service
@RequiredArgsConstructor
public class ConsultarRelatorioLucroUseCase {

    static final int PRODUTOS_NO_RELATORIO = 10;

    private final CalculadoraResultado calculadora;
    private final RelatorioRepository relatorioRepository;
    private final DespesaRepository despesaRepository;
    private final Clock clock;

    /** Uma transação só leitura: todas as consultas enxergam o mesmo retrato do banco. */
    @Transactional(readOnly = true)
    public RelatorioLucroDTO consultar(Long negocioId, FiltroPeriodoDTO filtro) {
        IntervaloRelatorio intervalo = IntervaloRelatorio.resolver(filtro.periodo(), filtro.data(),
                filtro.de(), filtro.ate(), LocalDate.now(clock));
        Periodo periodo = intervalo.periodo();
        SerieDTO serie = calculadora.serie(negocioId, intervalo, intervalo.agrupamentoPadrao());
        return new RelatorioLucroDTO(
                intervalo.tipo(),
                DescricaoPeriodo.de(intervalo),
                intervalo.de(),
                intervalo.ate(),
                serie.total(),
                serie.agrupamento(),
                serie.pontos(),
                despesaRepository.totaisPorTipo(negocioId, periodo.inicioData(), periodo.fimData()),
                relatorioRepository.perdasPorMotivo(negocioId, periodo),
                relatorioRepository.produtosMaisVendidos(negocioId, periodo, CriterioRanking.VENDAS,
                        PRODUTOS_NO_RELATORIO));
    }
}
