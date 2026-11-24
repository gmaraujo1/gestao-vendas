package br.com.gestao.gestaogastos.produto.application;

import br.com.gestao.gestaogastos.produto.dto.ProdutoFiltroDTO;
import br.com.gestao.gestaogastos.produto.dto.ProdutoResponseDTO;
import br.com.gestao.gestaogastos.produto.mapper.ProdutoMapper;
import br.com.gestao.gestaogastos.produto.repository.ProdutoRepository;
import br.com.gestao.gestaogastos.produto.repository.ProdutoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarProdutoUseCase {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    public List<ProdutoResponseDTO> listar(ProdutoFiltroDTO filtro) {
        return produtoRepository.findAll(ProdutoSpecification.comFiltros(filtro))
                .stream()
                .map(produtoMapper::toResponse)
                .toList();
    }
}
