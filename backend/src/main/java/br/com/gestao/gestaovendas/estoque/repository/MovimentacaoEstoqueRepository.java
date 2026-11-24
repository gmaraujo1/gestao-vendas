package br.com.gestao.gestaovendas.estoque.repository;

import br.com.gestao.gestaovendas.estoque.domain.MovimentacaoEstoque;
import br.com.gestao.gestaovendas.estoque.domain.TipoMovimentacao;
import br.com.gestao.gestaovendas.estoque.dto.SaldoProdutoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    @Query("""
            select coalesce(sum(m.quantidade), 0) from MovimentacaoEstoque m
            where m.negocioId = :negocioId and m.produtoId = :produtoId
            """)
    BigDecimal saldo(@Param("negocioId") Long negocioId, @Param("produtoId") Long produtoId);

    /** Saldo de todos os produtos do negócio (inclusive os sem movimentação, com 0). */
    @Query(value = """
            select new br.com.gestao.gestaovendas.estoque.dto.SaldoProdutoDTO(
                p.id, p.codigo, p.descricao, p.unidadeMedida, c.nome, p.ativo, coalesce(sum(m.quantidade), 0))
            from Produto p
            join p.categoria c
            left join MovimentacaoEstoque m on m.produtoId = p.id and m.negocioId = p.negocioId
            where p.negocioId = :negocioId
              and (:categoriaId is null or c.id = :categoriaId)
              and (:ativo is null or p.ativo = :ativo)
              and (:descricao is null or lower(p.descricao) like lower(concat('%', cast(:descricao as string), '%')) escape '\\')
            group by p.id, p.codigo, p.descricao, p.unidadeMedida, c.nome, p.ativo
            """,
            countQuery = """
            select count(p) from Produto p
            where p.negocioId = :negocioId
              and (:categoriaId is null or p.categoria.id = :categoriaId)
              and (:ativo is null or p.ativo = :ativo)
              and (:descricao is null or lower(p.descricao) like lower(concat('%', cast(:descricao as string), '%')) escape '\\')
            """)
    Page<SaldoProdutoDTO> saldos(@Param("negocioId") Long negocioId,
                                 @Param("categoriaId") Long categoriaId,
                                 @Param("ativo") Boolean ativo,
                                 @Param("descricao") String descricao,
                                 Pageable pageable);

    Page<MovimentacaoEstoque> findByNegocioIdAndProdutoId(Long negocioId, Long produtoId, Pageable pageable);

    /** Movimentações de alguns tipos em [de, ate) (ex.: perdas). Use {@code Periodo} para os limites. */
    @Query("""
            select m from MovimentacaoEstoque m
            where m.negocioId = :negocioId and m.tipo in :tipos
              and m.dataHora >= :de and m.dataHora < :ate
            """)
    Page<MovimentacaoEstoque> buscarPorTipo(@Param("negocioId") Long negocioId,
                                            @Param("tipos") Collection<TipoMovimentacao> tipos,
                                            @Param("de") LocalDateTime de,
                                            @Param("ate") LocalDateTime ate,
                                            Pageable pageable);

    List<MovimentacaoEstoque> findByVendaIdOrderByIdAsc(Long vendaId);

    List<MovimentacaoEstoque> findByTrocaIdOrderByIdAsc(Long trocaId);

    boolean existsByProdutoId(Long produtoId);

    /** Trava transacional por produto (liberada no commit/rollback). Ver MovimentacaoService.travar. */
    @Query(value = "select 1 from (select pg_advisory_xact_lock(:produtoId)) t", nativeQuery = true)
    Integer travarProduto(@Param("produtoId") Long produtoId);
}
