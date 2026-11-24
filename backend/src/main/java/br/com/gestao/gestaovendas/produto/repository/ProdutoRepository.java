package br.com.gestao.gestaovendas.produto.repository;

import br.com.gestao.gestaovendas.produto.domain.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    boolean existsByNegocioIdAndCodigo(Long negocioId, Integer codigo);

    Optional<Produto> findByIdAndNegocioId(Long id, Long negocioId);

}
