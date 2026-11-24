package br.com.gestao.gestaovendas.veiculo.application;

import br.com.gestao.gestaovendas.despesa.repository.DespesaRepository;
import br.com.gestao.gestaovendas.exception.ConflictException;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.veiculo.domain.Veiculo;
import br.com.gestao.gestaovendas.veiculo.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoverVeiculoUseCase {

    private final VeiculoRepository veiculoRepository;
    private final DespesaRepository despesaRepository;

    /** Veículo com despesa não é removido (perderia o histórico): desative. */
    @Transactional
    public void remover(Long id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));
        if (despesaRepository.existsByVeiculoId(veiculo.getId())) {
            throw new ConflictException("O veículo tem despesas. Desative-o em vez de excluir.");
        }
        veiculoRepository.delete(veiculo);
    }
}
