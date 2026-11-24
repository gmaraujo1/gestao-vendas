package br.com.gestao.gestaovendas.estoque.application;

import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.common.dto.Paginas;
import br.com.gestao.gestaovendas.common.text.Like;
import br.com.gestao.gestaovendas.estoque.dto.ExtratoProdutoDTO;
import br.com.gestao.gestaovendas.estoque.dto.MovimentacaoResponseDTO;
import br.com.gestao.gestaovendas.estoque.dto.SaldoProdutoDTO;
import br.com.gestao.gestaovendas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.domain.ProdutosDoNegocio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConsultarEstoqueUseCase {

    private static final Set<String> ORDENACAO_SALDO = Set.of("descricao", "codigo", "id");
    private static final Set<String> ORDENACAO_EXTRATO = Set.of("dataHora", "id");

    private final MovimentacaoEstoqueRepository repository;
    private final ProdutosDoNegocio produtosDoNegocio;

    @Transactional(readOnly = true)
    public PaginaDTO<SaldoProdutoDTO> saldos(Long negocioId, Long categoriaId, Boolean ativo, String descricao,
                                             Pageable pageable) {
        String termo = (descricao == null || descricao.isBlank()) ? null : Like.escapar(descricao.trim());
        var pagina = repository.saldos(negocioId, categoriaId, ativo, termo,
                Paginas.validar(pageable, ORDENACAO_SALDO));
        return PaginaDTO.de(pagina, s -> s);
    }

    @Transactional(readOnly = true)
    public ExtratoProdutoDTO extrato(Long negocioId, Long produtoId, Pageable pageable) {
        Produto produto = produtosDoNegocio.carregar(negocioId, produtoId, false);
        var movimentacoes = repository.findByNegocioIdAndProdutoId(negocioId, produtoId,
                Paginas.validar(pageable, ORDENACAO_EXTRATO));
        return new ExtratoProdutoDTO(produto.getId(), produto.getDescricao(),
                repository.saldo(negocioId, produtoId),
                PaginaDTO.de(movimentacoes, MovimentacaoResponseDTO::de));
    }
}
