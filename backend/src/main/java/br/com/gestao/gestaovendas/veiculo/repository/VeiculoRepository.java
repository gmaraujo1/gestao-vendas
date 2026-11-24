package br.com.gestao.gestaovendas.veiculo.repository;

import br.com.gestao.gestaovendas.veiculo.domain.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    List<Veiculo> findAllByOrderByDescricaoAsc();

    List<Veiculo> findAllByAtivoTrueOrderByDescricaoAsc();

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndIdNot(String placa, Long id);
}
