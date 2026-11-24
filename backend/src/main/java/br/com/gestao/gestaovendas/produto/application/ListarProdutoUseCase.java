package br.com.gestao.gestaovendas.produto.application;

import br.com.gestao.gestaovendas.produto.dto.ProdutoFiltroDTO;
import br.com.gestao.gestaovendas.produto.dto.ProdutoResponseDTO;
import br.com.gestao.gestaovendas.produto.mapper.ProdutoMapper;
import br.com.gestao.gestaovendas.produto.repository.ProdutoRepository;
import br.com.gestao.gestaovendas.produto.repository.ProdutoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarProdutoUseCase {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    public List<ProdutoResponseDTO> listar(Long negocioId, ProdutoFiltroDTO filtro) {
        return produtoRepository.findAll(ProdutoSpecification.comFiltros(negocioId, filtro))
                .stream()
                .map(produtoMapper::toResponse)
                .toList();
    }
}
