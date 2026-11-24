package br.com.gestao.gestaovendas.cliente.domain;

import br.com.gestao.gestaovendas.cliente.repository.ClienteRepository;
import br.com.gestao.gestaovendas.exception.BusinessException;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientesDoNegocio {

    private final ClienteRepository clienteRepository;

    /** Cliente do negócio e ativo (para vendas e trocas novas). */
    public Cliente ativo(Long negocioId, Long clienteId) {
        Cliente cliente = clienteRepository.findByIdAndNegocioId(clienteId, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        if (!cliente.isAtivo()) {
            throw new BusinessException("Cliente inativo: " + cliente.getNome());
        }
        return cliente;
    }
}
