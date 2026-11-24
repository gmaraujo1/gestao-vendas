package br.com.gestao.gestaovendas.cliente.application;

import br.com.gestao.gestaovendas.cliente.domain.Cliente;
import br.com.gestao.gestaovendas.cliente.repository.ClienteRepository;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoverClienteUseCase {

    private final ClienteRepository clienteRepository;

    /**
     * Remove de vez. Na etapa 3, cliente com venda não poderá ser removido (só desativado);
     * a FK da venda vai garantir isso.
     */
    @Transactional
    public void remover(Long negocioId, Long id) {
        Cliente cliente = clienteRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        clienteRepository.delete(cliente);
    }
}
