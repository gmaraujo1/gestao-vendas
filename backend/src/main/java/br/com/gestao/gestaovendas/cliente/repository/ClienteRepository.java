package br.com.gestao.gestaovendas.cliente.repository;

import br.com.gestao.gestaovendas.cliente.domain.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdAndNegocioId(Long id, Long negocioId);

    /** nome e ativo são filtros opcionais (null = não filtra). */
    @Query("""
            select c from Cliente c
            where c.negocioId = :negocioId
              and (:nome is null or lower(c.nome) like lower(concat('%', cast(:nome as string), '%')) escape '\\')
              and (:ativo is null or c.ativo = :ativo)
            """)
    Page<Cliente> buscar(@Param("negocioId") Long negocioId,
                         @Param("nome") String nome,
                         @Param("ativo") Boolean ativo,
                         Pageable pageable);
}
