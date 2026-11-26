package br.com.gestao.gestaovendas.relatorio.application;

import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.negocio.domain.Negocio;
import br.com.gestao.gestaovendas.negocio.repository.NegocioRepository;
import br.com.gestao.gestaovendas.relatorio.dto.FiltroPeriodoDTO;
import br.com.gestao.gestaovendas.relatorio.dto.RelatorioLucroDTO;
import br.com.gestao.gestaovendas.relatorio.pdf.RelatorioLucroPdf;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GerarRelatorioLucroPdfUseCase {

    private final ConsultarRelatorioLucroUseCase consultarRelatorioLucroUseCase;
    private final NegocioRepository negocioRepository;
    private final RelatorioLucroPdf relatorioLucroPdf;
    private final Clock clock;

    public record ArquivoPdf(String nome, byte[] conteudo) {
    }

    public ArquivoPdf gerar(Long negocioId, FiltroPeriodoDTO filtro) {
        RelatorioLucroDTO relatorio = consultarRelatorioLucroUseCase.consultar(negocioId, filtro);
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negócio não encontrado"));
        byte[] pdf = relatorioLucroPdf.gerar(negocio.getNome(), relatorio, LocalDateTime.now(clock));
        // só ASCII no nome do arquivo: o código do negócio (LATICINIO) e as datas ISO
        String nome = "lucro-%s-%s_%s.pdf".formatted(negocio.getCodigo().toLowerCase(Locale.ROOT),
                relatorio.de(), relatorio.ate());
        return new ArquivoPdf(nome, pdf);
    }
}
