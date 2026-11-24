package br.com.gestao.gestaovendas.produto.repository;

import br.com.gestao.gestaovendas.produto.domain.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    boolean existsByNegocioIdAndCodigo(Long negocioId, Integer codigo);

    boolean existsByCategoriaId(Long categoriaId);

    List<Produto> findAllByIdInAndNegocioId(Collection<Long> ids, Long negocioId);

    @EntityGraph(attributePaths = "categoria")
    Optional<Produto> findByIdAndNegocioId(Long id, Long negocioId);

    /** Traz a categoria junto: a listagem mostra o nome dela sem N+1. */
    @Override
    @EntityGraph(attributePaths = "categoria")
    Page<Produto> findAll(Specification<Produto> spec, Pageable pageable);
}
