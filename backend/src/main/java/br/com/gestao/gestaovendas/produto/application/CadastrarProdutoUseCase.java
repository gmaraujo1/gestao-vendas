package br.com.gestao.gestaovendas.produto.application;

import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.domain.ProdutoValidator;
import br.com.gestao.gestaovendas.produto.dto.ProdutoRequestDTO;
import br.com.gestao.gestaovendas.produto.dto.ProdutoResponseDTO;
import br.com.gestao.gestaovendas.produto.mapper.ProdutoMapper;
import br.com.gestao.gestaovendas.produto.repository.ProdutoRepository;
import jakarta.transaction.Transactional;
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
    public ProdutoResponseDTO cadastrar(ProdutoRequestDTO request) {
        produtoValidator.validaCodigoProduto(request.getCodigo());

        Produto novoProduto = produtoMapper.toEntity(request);
        novoProduto.setCriadoEm(LocalDateTime.now());
        novoProduto.setAtualizadoEm(LocalDateTime.now());

        Produto salvo = produtoRepository.save(novoProduto);

        return produtoMapper.toResponse(salvo);
    }
}
