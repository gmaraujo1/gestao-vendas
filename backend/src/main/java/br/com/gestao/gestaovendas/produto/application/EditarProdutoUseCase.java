package br.com.gestao.gestaovendas.produto.application;

import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.dto.ProdutoRequestEditDTO;
import br.com.gestao.gestaovendas.produto.dto.ProdutoResponseDTO;
import br.com.gestao.gestaovendas.produto.mapper.ProdutoMapper;
import br.com.gestao.gestaovendas.produto.repository.ProdutoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EditarProdutoUseCase {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    @Transactional
    public ProdutoResponseDTO atualizar(Long negocioId, Long id, ProdutoRequestEditDTO request) {
        Produto produto = produtoRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o produto"));

        produtoMapper.updateEntity(produto, request);
        produto.setAtualizadoEm(LocalDateTime.now());
        Produto atualizado = produtoRepository.save(produto);

        return produtoMapper.toResponse(atualizado);
    }
}
