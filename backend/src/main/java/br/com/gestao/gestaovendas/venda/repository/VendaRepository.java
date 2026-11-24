package br.com.gestao.gestaovendas.venda.repository;

import br.com.gestao.gestaovendas.venda.domain.StatusVenda;
import br.com.gestao.gestaovendas.venda.domain.Venda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    @EntityGraph(attributePaths = {"cliente", "itens", "itens.produto"})
    Optional<Venda> findByIdAndNegocioId(Long id, Long negocioId);

    @EntityGraph(attributePaths = "cliente")
    @Query("""
            select v from Venda v
            where v.negocioId = :negocioId
              and v.dataHora >= :de and v.dataHora < :ate
              and (:clienteId is null or v.cliente.id = :clienteId)
              and (:status is null or v.status = :status)
            """)
    Page<Venda> buscar(@Param("negocioId") Long negocioId,
                       @Param("de") LocalDateTime de,
                       @Param("ate") LocalDateTime ate,
                       @Param("clienteId") Long clienteId,
                       @Param("status") StatusVenda status,
                       Pageable pageable);

    boolean existsByClienteId(Long clienteId);

    /**
     * Para cancelar: trava a linha da venda (SELECT ... FOR UPDATE). Dois cancelamentos
     * simultâneos (clique duplo, retry do app) ficam em fila e o segundo vê CANCELADA.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Venda v where v.id = :id and v.negocioId = :negocioId")
    Optional<Venda> travarParaCancelar(@Param("id") Long id, @Param("negocioId") Long negocioId);
}
