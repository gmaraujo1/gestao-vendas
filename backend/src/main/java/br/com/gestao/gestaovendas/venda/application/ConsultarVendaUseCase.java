package br.com.gestao.gestaovendas.venda.application;

import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.common.dto.Paginas;
import br.com.gestao.gestaovendas.common.dto.Periodo;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.venda.domain.StatusVenda;
import br.com.gestao.gestaovendas.venda.dto.VendaResponseDTO;
import br.com.gestao.gestaovendas.venda.dto.VendaResumoDTO;
import br.com.gestao.gestaovendas.venda.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConsultarVendaUseCase {

    private static final Set<String> ORDENACAO = Set.of("dataHora", "total", "id");

    private final VendaRepository vendaRepository;

    /** Ex.: "o que o Marcelo levou esta semana": ?clienteId=..&de=..&ate=.. */
    @Transactional(readOnly = true)
    public PaginaDTO<VendaResumoDTO> listar(Long negocioId, LocalDate de, LocalDate ate, Long clienteId,
                                            StatusVenda status, Pageable pageable) {
        Periodo periodo = Periodo.de(de, ate);
        var pagina = vendaRepository.buscar(negocioId, periodo.inicio(), periodo.fim(), clienteId, status,
                Paginas.validar(pageable, ORDENACAO));
        return PaginaDTO.de(pagina, VendaResumoDTO::de);
    }

    @Transactional(readOnly = true)
    public VendaResponseDTO buscar(Long negocioId, Long id) {
        return vendaRepository.findByIdAndNegocioId(id, negocioId)
                .map(VendaResponseDTO::de)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada"));
    }
}
