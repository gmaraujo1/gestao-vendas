package br.com.gestao.gestaovendas.relatorio.controller;

import br.com.gestao.gestaovendas.negocio.context.NegocioAtual;
import br.com.gestao.gestaovendas.relatorio.application.ConsultarDashboardUseCase;
import br.com.gestao.gestaovendas.relatorio.domain.CriterioRanking;
import br.com.gestao.gestaovendas.relatorio.domain.TipoPeriodo;
import br.com.gestao.gestaovendas.relatorio.dto.FiltroPeriodoDTO;
import br.com.gestao.gestaovendas.relatorio.dto.ProdutoVendidoDTO;
import br.com.gestao.gestaovendas.relatorio.dto.ResumoPerdasDTO;
import br.com.gestao.gestaovendas.relatorio.dto.SerieDTO;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Dados dos gráficos (Recharts). O período segue as regras do relatório
 * ({@code periodo}+{@code data} ou {@code de}+{@code ate}). Despesas por tipo e por veículo
 * já existem em {@code GET /api/despesa/resumo}. Só ADMIN (SecurityConfig).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ConsultarDashboardUseCase consultarDashboardUseCase;

    /** Vendas, custo, despesas, perdas e resultado por dia/semana/mês (sem agrupamento: automático). */
    @GetMapping("/serie")
    public SerieDTO serie(NegocioAtual negocio, @ParameterObject FiltroPeriodoDTO filtro,
                          @RequestParam(required = false) TipoPeriodo agrupamento) {
        return consultarDashboardUseCase.serie(negocio.id(), filtro, agrupamento);
    }

    @GetMapping("/produtos-mais-vendidos")
    public List<ProdutoVendidoDTO> produtosMaisVendidos(
            NegocioAtual negocio, @ParameterObject FiltroPeriodoDTO filtro,
            @RequestParam(required = false) CriterioRanking criterio,
            @RequestParam(defaultValue = "10") int limite) {
        return consultarDashboardUseCase.produtosMaisVendidos(negocio.id(), filtro, criterio, limite);
    }

    /** Custo das perdas por motivo e os produtos que mais se perderam. */
    @GetMapping("/perdas")
    public ResumoPerdasDTO perdas(NegocioAtual negocio, @ParameterObject FiltroPeriodoDTO filtro,
                                  @RequestParam(defaultValue = "10") int limite) {
        return consultarDashboardUseCase.perdas(negocio.id(), filtro, limite);
    }
}
