package br.com.gestao.gestaovendas.relatorio.application;

import br.com.gestao.gestaovendas.common.dto.Periodo;
import br.com.gestao.gestaovendas.exception.RequisicaoInvalidaException;
import br.com.gestao.gestaovendas.relatorio.domain.CriterioRanking;
import br.com.gestao.gestaovendas.relatorio.domain.IntervaloRelatorio;
import br.com.gestao.gestaovendas.relatorio.domain.TipoPeriodo;
import br.com.gestao.gestaovendas.relatorio.dto.FiltroPeriodoDTO;
import br.com.gestao.gestaovendas.relatorio.dto.PerdaPorMotivoDTO;
import br.com.gestao.gestaovendas.relatorio.dto.ProdutoVendidoDTO;
import br.com.gestao.gestaovendas.relatorio.dto.ResumoPerdasDTO;
import br.com.gestao.gestaovendas.relatorio.dto.SerieDTO;
import br.com.gestao.gestaovendas.relatorio.repository.RelatorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Dados dos gráficos do dashboard, em JSON. */
@Service
@RequiredArgsConstructor
public class ConsultarDashboardUseCase {

    static final int LIMITE_MAXIMO = 50;

    private final CalculadoraResultado calculadora;
    private final RelatorioRepository repository;
    private final Clock clock;

    /** Série de vendas, custo, despesas, perdas e resultado. Sem agrupamento, escolhe pelo tamanho do intervalo. */
    @Transactional(readOnly = true)
    public SerieDTO serie(Long negocioId, FiltroPeriodoDTO filtro, TipoPeriodo agrupamento) {
        IntervaloRelatorio intervalo = intervalo(filtro);
        return calculadora.serie(negocioId, intervalo,
                agrupamento == null ? intervalo.agrupamentoPadrao() : agrupamento);
    }

    @Transactional(readOnly = true)
    public List<ProdutoVendidoDTO> produtosMaisVendidos(Long negocioId, FiltroPeriodoDTO filtro,
                                                        CriterioRanking criterio, int limite) {
        validarLimite(limite);
        return repository.produtosMaisVendidos(negocioId, intervalo(filtro).periodo(),
                criterio == null ? CriterioRanking.QUANTIDADE : criterio, limite);
    }

    @Transactional(readOnly = true)
    public ResumoPerdasDTO perdas(Long negocioId, FiltroPeriodoDTO filtro, int limite) {
        validarLimite(limite);
        IntervaloRelatorio intervalo = intervalo(filtro);
        Periodo periodo = intervalo.periodo();
        List<PerdaPorMotivoDTO> porMotivo = repository.perdasPorMotivo(negocioId, periodo);
        BigDecimal total = porMotivo.stream().map(PerdaPorMotivoDTO::custo).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumoPerdasDTO(intervalo.de(), intervalo.ate(), total, porMotivo,
                repository.perdasPorProduto(negocioId, periodo, limite));
    }

    private IntervaloRelatorio intervalo(FiltroPeriodoDTO filtro) {
        return IntervaloRelatorio.resolver(filtro.periodo(), filtro.data(), filtro.de(), filtro.ate(),
                LocalDate.now(clock));
    }

    private static void validarLimite(int limite) {
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new RequisicaoInvalidaException("O limite deve ficar entre 1 e " + LIMITE_MAXIMO);
        }
    }
}
