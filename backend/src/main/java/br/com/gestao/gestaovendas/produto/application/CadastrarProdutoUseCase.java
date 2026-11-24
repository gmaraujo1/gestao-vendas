package br.com.gestao.gestaovendas.produto.application;

import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.domain.ProdutoValidator;
import br.com.gestao.gestaovendas.produto.dto.ProdutoRequestDTO;
import br.com.gestao.gestaovendas.produto.dto.ProdutoResponseDTO;
import br.com.gestao.gestaovendas.produto.mapper.ProdutoMapper;
import br.com.gestao.gestaovendas.produto.repository.ProdutoRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CadastrarProdutoUseCase {

    private final ProdutoRepository produtoRepository;
    private final ProdutoValidator produtoValidator;
    private final ProdutoMapper produtoMapper;

    @Transactional
    public ProdutoResponseDTO cadastrar(Long negocioId, ProdutoRequestDTO request) {
        produtoValidator.validaCodigoProduto(negocioId, request.getCodigo());

        Produto novoProduto = produtoMapper.toEntity(request);
        novoProduto.setNegocioId(negocioId);
        novoProduto.setCriadoEm(LocalDateTime.now());
        novoProduto.setAtualizadoEm(LocalDateTime.now());

        Produto salvo = produtoRepository.save(novoProduto);

        return produtoMapper.toResponse(salvo);
    }
}
