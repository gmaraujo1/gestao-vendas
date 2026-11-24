package br.com.gestao.gestaovendas.despesa.domain;

import br.com.gestao.gestaovendas.exception.BusinessException;
import br.com.gestao.gestaovendas.exception.RequisicaoInvalidaException;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.veiculo.domain.Veiculo;
import br.com.gestao.gestaovendas.veiculo.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DespesaValidator {

    private final VeiculoRepository veiculoRepository;
    private final Clock clock;

    public Veiculo veiculoAtivo(Long veiculoId) {
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));
        if (!veiculo.isAtivo()) {
            throw new BusinessException("Veículo inativo: " + veiculo.getDescricao());
        }
        return veiculo;
    }

    /** Sem data = hoje. Pode ser no passado, não no futuro. */
    public LocalDate data(LocalDate informada) {
        LocalDate hoje = LocalDate.now(clock);
        if (informada == null) {
            return hoje;
        }
        if (informada.isAfter(hoje)) {
            throw new RequisicaoInvalidaException("A data da despesa não pode ser no futuro");
        }
        return informada;
    }
}
