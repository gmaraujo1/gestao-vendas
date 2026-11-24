package br.com.gestao.gestaovendas.categoria.repository;

import br.com.gestao.gestaovendas.categoria.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByNegocioIdOrderByNomeAsc(Long negocioId);

    List<Categoria> findAllByNegocioIdAndAtivoTrueOrderByNomeAsc(Long negocioId);

    Optional<Categoria> findByIdAndNegocioId(Long id, Long negocioId);

    boolean existsByNegocioIdAndNomeIgnoreCase(Long negocioId, String nome);

    boolean existsByNegocioIdAndNomeIgnoreCaseAndIdNot(Long negocioId, String nome, Long id);
}
