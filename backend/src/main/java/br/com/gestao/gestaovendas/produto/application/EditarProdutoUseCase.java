package br.com.gestao.gestaovendas.produto.application;

import br.com.gestao.gestaovendas.common.text.Textos;
import br.com.gestao.gestaovendas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.gestao.gestaovendas.exception.BusinessException;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.domain.ProdutoValidator;
import br.com.gestao.gestaovendas.produto.dto.ProdutoRequestEditDTO;
import br.com.gestao.gestaovendas.produto.dto.ProdutoResponseDTO;
import br.com.gestao.gestaovendas.produto.mapper.ProdutoMapper;
import br.com.gestao.gestaovendas.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EditarProdutoUseCase {

    private final ProdutoRepository produtoRepository;
    private final ProdutoValidator produtoValidator;
    private final ProdutoMapper produtoMapper;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    @Transactional
    public ProdutoResponseDTO atualizar(Long negocioId, Long id, ProdutoRequestEditDTO request) {
        Produto produto = produtoRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o produto"));

        // trocar a unidade (ex.: L -> CX) depois de haver movimentação corromperia os saldos
        if (request.unidadeMedida() != null && request.unidadeMedida() != produto.getUnidadeMedida()
                && movimentacaoRepository.existsByProdutoId(produto.getId())) {
            throw new BusinessException("Não é possível mudar a unidade de um produto que já tem movimentação. "
                    + "Cadastre um produto novo.");
        }
        produtoMapper.updateEntity(produto, request);
        // só valida quando troca: a categoria atual pode ter sido desativada depois
        if (request.categoriaId() != null && !request.categoriaId().equals(produto.getCategoria().getId())) {
            produto.setCategoria(produtoValidator.categoriaDoNegocio(negocioId, request.categoriaId()));
        }
        produto.setDescricao(produto.getDescricao().trim());
        produto.setObservacao(Textos.limpar(produto.getObservacao()));
        return produtoMapper.toResponse(produto);
    }
}
