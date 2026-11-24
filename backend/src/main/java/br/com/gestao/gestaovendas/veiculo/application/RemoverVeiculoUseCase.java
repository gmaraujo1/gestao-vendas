package br.com.gestao.gestaovendas.veiculo.application;

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

    /** Na etapa 4, veículo com despesa não poderá ser removido (só desativado); a FK garante. */
    @Transactional
    public void remover(Long id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));
        veiculoRepository.delete(veiculo);
    }
}
