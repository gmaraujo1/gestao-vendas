package br.com.gestao.gestaogastos.produto.application;

import br.com.gestao.gestaogastos.exception.ResourceNotFoundException;
import br.com.gestao.gestaogastos.produto.domain.Produto;
import br.com.gestao.gestaogastos.produto.dto.ProdutoRequestEditDTO;
import br.com.gestao.gestaogastos.produto.dto.ProdutoResponseDTO;
import br.com.gestao.gestaogastos.produto.mapper.ProdutoMapper;
import br.com.gestao.gestaogastos.produto.repository.ProdutoRepository;
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
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestEditDTO request) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o produto"));

        produtoMapper.updateEntity(produto, request);
        produto.setAtualizadoEm(LocalDateTime.now());
        Produto atualizado = produtoRepository.save(produto);

        return produtoMapper.toResponse(atualizado);
    }
}
