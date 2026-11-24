package br.com.gestao.gestaovendas.produto.domain;

import br.com.gestao.gestaovendas.exception.BusinessException;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Carrega produtos garantindo que todos são do negócio (e, quando pedido, ativos). */
@Service
@RequiredArgsConstructor
public class ProdutosDoNegocio {

    private final ProdutoRepository produtoRepository;

    public Map<Long, Produto> carregar(Long negocioId, Collection<Long> ids, boolean exigirAtivo) {
        Set<Long> distintos = new HashSet<>(ids);
        Map<Long, Produto> produtos = produtoRepository.findAllByIdInAndNegocioId(distintos, negocioId).stream()
                .collect(Collectors.toMap(Produto::getId, Function.identity()));
        for (Long id : distintos) {
            Produto p = produtos.get(id);
            if (p == null) {
                throw new ResourceNotFoundException("Produto " + id + " não encontrado");
            }
            if (exigirAtivo && !p.isAtivo()) {
                throw new BusinessException("Produto inativo: " + p.getDescricao());
            }
        }
        return produtos;
    }

    public Produto carregar(Long negocioId, Long id, boolean exigirAtivo) {
        return carregar(negocioId, Set.of(id), exigirAtivo).get(id);
    }
}
