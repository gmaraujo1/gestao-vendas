package br.com.gestao.gestaovendas.produto.application;

import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.common.dto.Paginas;
import br.com.gestao.gestaovendas.produto.dto.ProdutoFiltroDTO;
import br.com.gestao.gestaovendas.produto.dto.ProdutoResponseDTO;
import br.com.gestao.gestaovendas.produto.mapper.ProdutoMapper;
import br.com.gestao.gestaovendas.produto.repository.ProdutoRepository;
import br.com.gestao.gestaovendas.produto.repository.ProdutoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListarProdutoUseCase {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    private static final Set<String> ORDENACAO =
            Set.of("descricao", "codigo", "precoMedio", "criadoEm", "atualizadoEm", "id");

    @Transactional(readOnly = true)
    public PaginaDTO<ProdutoResponseDTO> listar(Long negocioId, ProdutoFiltroDTO filtro, Pageable pageable) {
        var pagina = produtoRepository.findAll(
                ProdutoSpecification.comFiltros(negocioId, filtro), Paginas.validar(pageable, ORDENACAO));
        return PaginaDTO.de(pagina, produtoMapper::toResponse);
    }
}
