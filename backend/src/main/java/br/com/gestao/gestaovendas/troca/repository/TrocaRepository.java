package br.com.gestao.gestaovendas.troca.repository;

import br.com.gestao.gestaovendas.troca.domain.Troca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TrocaRepository extends JpaRepository<Troca, Long> {

    @EntityGraph(attributePaths = "cliente")
    Optional<Troca> findByIdAndNegocioId(Long id, Long negocioId);

    @EntityGraph(attributePaths = "cliente")
    @Query("""
            select t from Troca t
            where t.negocioId = :negocioId
              and t.dataHora >= :de and t.dataHora < :ate
              and (:clienteId is null or t.cliente.id = :clienteId)
            """)
    Page<Troca> buscar(@Param("negocioId") Long negocioId,
                       @Param("de") LocalDateTime de,
                       @Param("ate") LocalDateTime ate,
                       @Param("clienteId") Long clienteId,
                       Pageable pageable);

    boolean existsByClienteId(Long clienteId);
}
