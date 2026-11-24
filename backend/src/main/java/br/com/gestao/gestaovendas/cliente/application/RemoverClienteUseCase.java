package br.com.gestao.gestaovendas.cliente.application;

import br.com.gestao.gestaovendas.cliente.domain.Cliente;
import br.com.gestao.gestaovendas.cliente.repository.ClienteRepository;
import br.com.gestao.gestaovendas.exception.ConflictException;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.troca.repository.TrocaRepository;
import br.com.gestao.gestaovendas.venda.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoverClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;
    private final TrocaRepository trocaRepository;

    /** Cliente com vendas ou trocas não é excluído (perderia o histórico): desative. */
    @Transactional
    public void remover(Long negocioId, Long id) {
        Cliente cliente = clienteRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        if (vendaRepository.existsByClienteId(cliente.getId()) || trocaRepository.existsByClienteId(cliente.getId())) {
            throw new ConflictException("O cliente tem vendas ou trocas. Desative-o em vez de excluir.");
        }
        clienteRepository.delete(cliente);
    }
}
