package br.com.gestao.gestaovendas.produto.domain;

import br.com.gestao.gestaovendas.categoria.domain.Categoria;
import br.com.gestao.gestaovendas.categoria.repository.CategoriaRepository;
import br.com.gestao.gestaovendas.exception.BusinessException;
import br.com.gestao.gestaovendas.exception.ConflictException;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProdutoValidator {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public void validaCodigoProduto(Long negocioId, Integer codigo) {
        if (produtoRepository.existsByNegocioIdAndCodigo(negocioId, codigo)) {
            throw new ConflictException("Código de produto já existente!");
        }
    }

    /** A categoria precisa ser do mesmo negócio (senão um negócio usaria a categoria do outro). */
    public Categoria categoriaDoNegocio(Long negocioId, Long categoriaId) {
        Categoria categoria = categoriaRepository.findByIdAndNegocioId(categoriaId, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        if (!categoria.isAtivo()) {
            throw new BusinessException("Categoria inativa");
        }
        return categoria;
    }
}
