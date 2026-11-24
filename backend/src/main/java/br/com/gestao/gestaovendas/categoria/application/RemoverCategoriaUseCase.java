package br.com.gestao.gestaovendas.categoria.application;

import br.com.gestao.gestaovendas.categoria.domain.Categoria;
import br.com.gestao.gestaovendas.categoria.repository.CategoriaRepository;
import br.com.gestao.gestaovendas.exception.ConflictException;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoverCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional
    public void remover(Long negocioId, Long id) {
        Categoria categoria = categoriaRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        if (produtoRepository.existsByCategoriaId(id)) {
            throw new ConflictException("A categoria tem produtos. Mova os produtos ou desative a categoria.");
        }
        categoriaRepository.delete(categoria);
    }
}
