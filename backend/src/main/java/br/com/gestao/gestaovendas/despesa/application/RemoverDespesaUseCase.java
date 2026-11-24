package br.com.gestao.gestaovendas.despesa.application;

import br.com.gestao.gestaovendas.despesa.domain.Despesa;
import br.com.gestao.gestaovendas.despesa.repository.DespesaRepository;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Só ADMIN. Despesa não mexe em estoque, então pode ser apagada (lançamento errado). */
@Service
@RequiredArgsConstructor
public class RemoverDespesaUseCase {

    private final DespesaRepository despesaRepository;

    @Transactional
    public void remover(Long negocioId, Long id) {
        Despesa despesa = despesaRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada"));
        despesaRepository.delete(despesa);
    }
}
