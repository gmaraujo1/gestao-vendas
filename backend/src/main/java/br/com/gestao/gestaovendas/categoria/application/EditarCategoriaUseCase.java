package br.com.gestao.gestaovendas.categoria.application;

import br.com.gestao.gestaovendas.categoria.domain.Categoria;
import br.com.gestao.gestaovendas.categoria.dto.CategoriaEditDTO;
import br.com.gestao.gestaovendas.categoria.dto.CategoriaResponseDTO;
import br.com.gestao.gestaovendas.categoria.mapper.CategoriaMapper;
import br.com.gestao.gestaovendas.categoria.repository.CategoriaRepository;
import br.com.gestao.gestaovendas.exception.ConflictException;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EditarCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Transactional
    public CategoriaResponseDTO editar(Long negocioId, Long id, CategoriaEditDTO request) {
        Categoria categoria = categoriaRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        if (request.nome() != null) {
            String nome = request.nome().trim();
            if (categoriaRepository.existsByNegocioIdAndNomeIgnoreCaseAndIdNot(negocioId, nome, id)) {
                throw new ConflictException("Já existe uma categoria com esse nome");
            }
            categoria.setNome(nome);
        }
        if (request.ativo() != null) {
            categoria.setAtivo(request.ativo());
        }
        return categoriaMapper.toResponse(categoria);
    }
}
