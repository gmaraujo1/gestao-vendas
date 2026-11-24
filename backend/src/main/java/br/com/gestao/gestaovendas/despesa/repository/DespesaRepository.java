package br.com.gestao.gestaovendas.despesa.repository;

import br.com.gestao.gestaovendas.despesa.domain.Despesa;
import br.com.gestao.gestaovendas.despesa.domain.TipoDespesa;
import br.com.gestao.gestaovendas.despesa.dto.ResumoDespesasDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    @EntityGraph(attributePaths = "veiculo")
    Optional<Despesa> findByIdAndNegocioId(Long id, Long negocioId);

    @EntityGraph(attributePaths = "veiculo")
    @Query("""
            select d from Despesa d
            where d.negocioId = :negocioId
              and d.data >= :de and d.data <= :ate
              and (:tipo is null or d.tipo = :tipo)
              and (:veiculoId is null or d.veiculo.id = :veiculoId)
            """)
    Page<Despesa> buscar(@Param("negocioId") Long negocioId,
                         @Param("de") LocalDate de,
                         @Param("ate") LocalDate ate,
                         @Param("tipo") TipoDespesa tipo,
                         @Param("veiculoId") Long veiculoId,
                         Pageable pageable);

    @Query("""
            select new br.com.gestao.gestaovendas.despesa.dto.ResumoDespesasDTO$PorTipo(d.tipo, sum(d.valor))
            from Despesa d
            where d.negocioId = :negocioId and d.data >= :de and d.data <= :ate
            group by d.tipo
            order by sum(d.valor) desc
            """)
    List<ResumoDespesasDTO.PorTipo> totaisPorTipo(@Param("negocioId") Long negocioId,
                                                 @Param("de") LocalDate de,
                                                 @Param("ate") LocalDate ate);

    @Query("""
            select new br.com.gestao.gestaovendas.despesa.dto.ResumoDespesasDTO$PorVeiculo(v.id, v.descricao, sum(d.valor))
            from Despesa d left join d.veiculo v
            where d.negocioId = :negocioId and d.data >= :de and d.data <= :ate
            group by v.id, v.descricao
            order by sum(d.valor) desc
            """)
    List<ResumoDespesasDTO.PorVeiculo> totaisPorVeiculo(@Param("negocioId") Long negocioId,
                                                       @Param("de") LocalDate de,
                                                       @Param("ate") LocalDate ate);

    boolean existsByVeiculoId(Long veiculoId);
}
