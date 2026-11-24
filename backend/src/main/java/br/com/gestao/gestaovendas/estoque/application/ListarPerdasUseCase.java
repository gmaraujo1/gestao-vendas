package br.com.gestao.gestaovendas.estoque.application;

import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.common.dto.Paginas;
import br.com.gestao.gestaovendas.common.dto.Periodo;
import br.com.gestao.gestaovendas.estoque.domain.TipoMovimentacao;
import br.com.gestao.gestaovendas.estoque.dto.MovimentacaoResponseDTO;
import br.com.gestao.gestaovendas.estoque.repository.MovimentacaoEstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

/** Perdas avulsas e as que vieram de trocas (devolvido que virou perda). */
@Service
@RequiredArgsConstructor
public class ListarPerdasUseCase {

    private static final Set<String> ORDENACAO = Set.of("dataHora", "id");

    private final MovimentacaoEstoqueRepository repository;

    @Transactional(readOnly = true)
    public PaginaDTO<MovimentacaoResponseDTO> listar(Long negocioId, LocalDate de, LocalDate ate, Pageable pageable) {
        Periodo periodo = Periodo.de(de, ate);
        var pagina = repository.buscarPorTipo(negocioId, Set.of(TipoMovimentacao.PERDA),
                periodo.inicio(), periodo.fim(), Paginas.validar(pageable, ORDENACAO));
        return PaginaDTO.de(pagina, MovimentacaoResponseDTO::de);
    }
}
