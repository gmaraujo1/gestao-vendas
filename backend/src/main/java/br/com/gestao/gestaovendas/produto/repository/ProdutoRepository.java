package br.com.gestao.gestaovendas.produto.repository;

import br.com.gestao.gestaovendas.produto.domain.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    Boolean existsByCodigo(Integer codigo);

}
