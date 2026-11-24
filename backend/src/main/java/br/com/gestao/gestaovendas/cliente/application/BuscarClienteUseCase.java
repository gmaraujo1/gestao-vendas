package br.com.gestao.gestaovendas.cliente.application;

import br.com.gestao.gestaovendas.cliente.dto.ClienteResponseDTO;
import br.com.gestao.gestaovendas.cliente.mapper.ClienteMapper;
import br.com.gestao.gestaovendas.cliente.repository.ClienteRepository;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuscarClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscar(Long negocioId, Long id) {
        return clienteRepository.findByIdAndNegocioId(id, negocioId)
                .map(clienteMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
    }
}
