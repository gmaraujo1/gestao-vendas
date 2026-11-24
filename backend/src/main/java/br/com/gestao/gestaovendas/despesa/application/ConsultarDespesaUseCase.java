package br.com.gestao.gestaovendas.despesa.application;

import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.common.dto.Paginas;
import br.com.gestao.gestaovendas.common.dto.Periodo;
import br.com.gestao.gestaovendas.despesa.domain.TipoDespesa;
import br.com.gestao.gestaovendas.despesa.dto.DespesaResponseDTO;
import br.com.gestao.gestaovendas.despesa.dto.ResumoDespesasDTO;
import br.com.gestao.gestaovendas.despesa.repository.DespesaRepository;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConsultarDespesaUseCase {

    private static final Set<String> ORDENACAO = Set.of("data", "valor", "tipo", "id");

    private final DespesaRepository despesaRepository;

    @Transactional(readOnly = true)
    public PaginaDTO<DespesaResponseDTO> listar(Long negocioId, LocalDate de, LocalDate ate, TipoDespesa tipo,
                                                Long veiculoId, Pageable pageable) {
        Periodo periodo = Periodo.de(de, ate);
        var pagina = despesaRepository.buscar(negocioId, periodo.inicioData(), periodo.fimData(), tipo, veiculoId,
                Paginas.validar(pageable, ORDENACAO));
        return PaginaDTO.de(pagina, DespesaResponseDTO::de);
    }

    @Transactional(readOnly = true)
    public DespesaResponseDTO buscar(Long negocioId, Long id) {
        return despesaRepository.findByIdAndNegocioId(id, negocioId)
                .map(DespesaResponseDTO::de)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada"));
    }

    @Transactional(readOnly = true)
    public ResumoDespesasDTO resumo(Long negocioId, LocalDate de, LocalDate ate) {
        Periodo periodo = Periodo.de(de, ate);
        var porTipo = despesaRepository.totaisPorTipo(negocioId, periodo.inicioData(), periodo.fimData());
        var porVeiculo = despesaRepository.totaisPorVeiculo(negocioId, periodo.inicioData(), periodo.fimData());
        BigDecimal total = porTipo.stream().map(ResumoDespesasDTO.PorTipo::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumoDespesasDTO(de, ate, total, porTipo, porVeiculo);
    }
}
