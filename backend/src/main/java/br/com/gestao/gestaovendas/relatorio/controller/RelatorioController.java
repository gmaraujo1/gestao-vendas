package br.com.gestao.gestaovendas.relatorio.controller;

import br.com.gestao.gestaovendas.negocio.context.NegocioAtual;
import br.com.gestao.gestaovendas.relatorio.application.ConsultarRelatorioLucroUseCase;
import br.com.gestao.gestaovendas.relatorio.application.GerarRelatorioLucroPdfUseCase;
import br.com.gestao.gestaovendas.relatorio.dto.FiltroPeriodoDTO;
import br.com.gestao.gestaovendas.relatorio.dto.RelatorioLucroDTO;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Relatório de lucro e prejuízo. Período: {@code ?periodo=MES&data=2026-11-10}
 * (DIA, SEMANA, QUINZENA, MES, TRIMESTRE, SEMESTRE ou ANO; sem data = hoje)
 * ou intervalo livre {@code ?de=2026-11-01&ate=2026-11-20}. Só ADMIN (SecurityConfig).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/relatorio")
public class RelatorioController {

    private final ConsultarRelatorioLucroUseCase consultarRelatorioLucroUseCase;
    private final GerarRelatorioLucroPdfUseCase gerarRelatorioLucroPdfUseCase;

    @GetMapping("/lucro")
    public RelatorioLucroDTO lucro(NegocioAtual negocio, @ParameterObject FiltroPeriodoDTO filtro) {
        return consultarRelatorioLucroUseCase.consultar(negocio.id(), filtro);
    }

    /** O mesmo relatório em PDF, para baixar ou compartilhar. */
    @GetMapping(value = "/lucro/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> lucroPdf(NegocioAtual negocio, @ParameterObject FiltroPeriodoDTO filtro) {
        var arquivo = gerarRelatorioLucroPdfUseCase.gerar(negocio.id(), filtro);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(arquivo.nome()).build().toString())
                .body(arquivo.conteudo());
    }
}
