package br.com.gestao.gestaovendas.categoria.application;

import br.com.gestao.gestaovendas.categoria.dto.CategoriaResponseDTO;
import br.com.gestao.gestaovendas.categoria.mapper.CategoriaMapper;
import br.com.gestao.gestaovendas.categoria.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    /** Poucas categorias por negócio: lista inteira, sem paginação. */
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar(Long negocioId, boolean apenasAtivas) {
        var categorias = apenasAtivas
                ? categoriaRepository.findAllByNegocioIdAndAtivoTrueOrderByNomeAsc(negocioId)
                : categoriaRepository.findAllByNegocioIdOrderByNomeAsc(negocioId);
        return categorias.stream().map(categoriaMapper::toResponse).toList();
    }
}
