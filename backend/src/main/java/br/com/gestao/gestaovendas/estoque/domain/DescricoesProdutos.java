package br.com.gestao.gestaovendas.estoque.domain;

import br.com.gestao.gestaovendas.estoque.dto.MovimentacaoResponseDTO;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Põe a descrição do produto em listas de movimentações, com uma consulta só (sem N+1). */
@Component
@RequiredArgsConstructor
public class DescricoesProdutos {

    private final ProdutoRepository produtoRepository;

    public Function<MovimentacaoEstoque, MovimentacaoResponseDTO> conversor(Long negocioId,
                                                                           Collection<MovimentacaoEstoque> movs) {
        var ids = movs.stream().map(MovimentacaoEstoque::getProdutoId).collect(Collectors.toSet());
        Map<Long, String> descricoes = ids.isEmpty() ? Map.of()
                : produtoRepository.findAllByIdInAndNegocioId(ids, negocioId).stream()
                        .collect(Collectors.toMap(Produto::getId, Produto::getDescricao));
        return m -> MovimentacaoResponseDTO.de(m, descricoes.get(m.getProdutoId()));
    }

    public List<MovimentacaoResponseDTO> converter(Long negocioId, List<MovimentacaoEstoque> movs) {
        return movs.stream().map(conversor(negocioId, movs)).toList();
    }
}
