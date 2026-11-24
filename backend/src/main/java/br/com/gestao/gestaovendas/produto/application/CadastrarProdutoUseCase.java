package br.com.gestao.gestaovendas.produto.application;

import br.com.gestao.gestaovendas.common.text.Textos;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.domain.ProdutoValidator;
import br.com.gestao.gestaovendas.produto.dto.ProdutoRequestDTO;
import br.com.gestao.gestaovendas.produto.dto.ProdutoResponseDTO;
import br.com.gestao.gestaovendas.produto.mapper.ProdutoMapper;
import br.com.gestao.gestaovendas.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CadastrarProdutoUseCase {

    private final ProdutoRepository produtoRepository;
    private final ProdutoValidator produtoValidator;
    private final ProdutoMapper produtoMapper;

    @Transactional
    public ProdutoResponseDTO cadastrar(Long negocioId, ProdutoRequestDTO request) {
        produtoValidator.validaCodigoProduto(negocioId, request.codigo());

        Produto novoProduto = produtoMapper.toEntity(request);
        novoProduto.setNegocioId(negocioId);
        novoProduto.setDescricao(novoProduto.getDescricao().trim());
        novoProduto.setObservacao(Textos.limpar(novoProduto.getObservacao()));
        novoProduto.setCategoria(produtoValidator.categoriaDoNegocio(negocioId, request.categoriaId()));

        return produtoMapper.toResponse(produtoRepository.save(novoProduto));
    }
}
