package br.com.gestao.gestaovendas.produto.application;

import br.com.gestao.gestaovendas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.gestao.gestaovendas.exception.ConflictException;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.repository.ProdutoRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemoverProdutoUseCase {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    @Transactional
    public void deletar(Long negocioId, Long id) {
        Produto produto = produtoRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o produto"));
        if (movimentacaoRepository.existsByProdutoId(produto.getId())) {
            throw new ConflictException("O produto tem histórico de estoque/vendas. Desative-o em vez de excluir.");
        }
        produtoRepository.delete(produto);
    }
}
