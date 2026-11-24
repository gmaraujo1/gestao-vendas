package br.com.gestao.gestaovendas.estoque.domain;

import br.com.gestao.gestaovendas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Único ponto que grava movimentações. Recebe a quantidade SEM sinal e aplica o sinal
 * pelo tipo, para que nenhum use case erre a direção do estoque.
 * O estoque pode ficar negativo (decisão dos donos): não há bloqueio por saldo.
 */
@Service
@RequiredArgsConstructor
public class MovimentacaoService {

    private final MovimentacaoEstoqueRepository repository;

    /**
     * Trava os produtos até o fim da transação (pg_advisory_xact_lock), sempre em ordem de id
     * para não haver deadlock. Todo use case que grava movimentação chama isto antes: assim o
     * ajuste ("informar estoque"), que lê o saldo e grava a diferença, nunca calcula a diferença
     * em cima de um saldo que uma venda concorrente está mudando.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void travar(Collection<Long> produtoIds) {
        new TreeSet<>(produtoIds).forEach(repository::travarProduto);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public MovimentacaoEstoque registrar(Movimento movimento) {
        BigDecimal quantidade = movimento.quantidade();
        if (quantidade == null || quantidade.signum() == 0) {
            throw new IllegalArgumentException("Quantidade da movimentação não pode ser zero");
        }
        BigDecimal comSinal = switch (movimento.tipo()) {
            case VENDA, PERDA, TROCA_SAIDA -> quantidade.abs().negate();
            case VENDA_ESTORNO, TROCA_ENTRADA, COMPRA -> quantidade.abs();
            case AJUSTE -> quantidade; // diferença já vem com sinal
        };
        Produto produto = movimento.produto();
        return repository.save(MovimentacaoEstoque.builder()
                .negocioId(produto.getNegocioId())
                .produtoId(produto.getId())
                .tipo(movimento.tipo())
                .quantidade(comSinal)
                .custoUnitario(movimento.custoUnitario() != null ? movimento.custoUnitario() : produto.getPrecoMedio())
                .dataHora(movimento.dataHora())
                .motivoPerda(movimento.tipo() == TipoMovimentacao.PERDA ? movimento.motivoPerda() : null)
                .observacao(movimento.observacao())
                .vendaId(movimento.vendaId())
                .trocaId(movimento.trocaId())
                .usuarioId(movimento.usuarioId())
                .build());
    }

    /** Dados de uma movimentação. Use os construtores estáticos para cada caso. */
    public record Movimento(
            Produto produto,
            TipoMovimentacao tipo,
            BigDecimal quantidade,
            LocalDateTime dataHora,
            MotivoPerda motivoPerda,
            String observacao,
            Long vendaId,
            Long trocaId,
            Long usuarioId,
            /* null = custo médio atual do produto */
            BigDecimal custoUnitario
    ) {
        public static Movimento venda(Produto p, BigDecimal qtd, LocalDateTime quando, Long vendaId, Long usuarioId) {
            return new Movimento(p, TipoMovimentacao.VENDA, qtd, quando, null, null, vendaId, null, usuarioId, null);
        }

        /** Usa o custo gravado na venda: o estorno anula exatamente o custo que a venda lançou. */
        public static Movimento estornoVenda(Produto p, BigDecimal qtd, LocalDateTime quando, Long vendaId,
                                             String motivo, Long usuarioId, BigDecimal custoDaVenda) {
            return new Movimento(p, TipoMovimentacao.VENDA_ESTORNO, qtd, quando, null, motivo, vendaId, null, usuarioId,
                    custoDaVenda);
        }

        public static Movimento perda(Produto p, BigDecimal qtd, LocalDateTime quando, MotivoPerda motivo,
                                      String observacao, Long trocaId, Long usuarioId) {
            return new Movimento(p, TipoMovimentacao.PERDA, qtd, quando, motivo, observacao, null, trocaId, usuarioId, null);
        }

        public static Movimento trocaEntrada(Produto p, BigDecimal qtd, LocalDateTime quando, Long trocaId, Long usuarioId) {
            return new Movimento(p, TipoMovimentacao.TROCA_ENTRADA, qtd, quando, null, null, null, trocaId, usuarioId, null);
        }

        public static Movimento trocaSaida(Produto p, BigDecimal qtd, LocalDateTime quando, Long trocaId, Long usuarioId) {
            return new Movimento(p, TipoMovimentacao.TROCA_SAIDA, qtd, quando, null, null, null, trocaId, usuarioId, null);
        }

        public static Movimento ajuste(Produto p, BigDecimal diferenca, LocalDateTime quando, String observacao,
                                       Long usuarioId) {
            return new Movimento(p, TipoMovimentacao.AJUSTE, diferenca, quando, null, observacao, null, null, usuarioId, null);
        }
    }
}
