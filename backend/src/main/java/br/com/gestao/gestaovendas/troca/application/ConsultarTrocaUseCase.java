package br.com.gestao.gestaovendas.troca.application;

import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.common.dto.Paginas;
import br.com.gestao.gestaovendas.common.dto.Periodo;
import br.com.gestao.gestaovendas.estoque.domain.DescricoesProdutos;
import br.com.gestao.gestaovendas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.troca.dto.TrocaResponseDTO;
import br.com.gestao.gestaovendas.troca.dto.TrocaResumoDTO;
import br.com.gestao.gestaovendas.troca.repository.TrocaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConsultarTrocaUseCase {

    private static final Set<String> ORDENACAO = Set.of("dataHora", "id");

    private final TrocaRepository trocaRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final DescricoesProdutos descricoesProdutos;

    @Transactional(readOnly = true)
    public PaginaDTO<TrocaResumoDTO> listar(Long negocioId, LocalDate de, LocalDate ate, Long clienteId,
                                              Pageable pageable) {
        Periodo periodo = Periodo.de(de, ate);
        var pagina = trocaRepository.buscar(negocioId, periodo.inicio(), periodo.fim(), clienteId,
                Paginas.validar(pageable, ORDENACAO));
        return PaginaDTO.de(pagina, TrocaResumoDTO::de);
    }

    @Transactional(readOnly = true)
    public TrocaResponseDTO buscar(Long negocioId, Long id) {
        var troca = trocaRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Troca não encontrada"));
        var movs = descricoesProdutos.converter(negocioId,
                movimentacaoRepository.findByTrocaIdOrderByIdAsc(troca.getId()));
        return TrocaResponseDTO.de(troca, movs);
    }
}
