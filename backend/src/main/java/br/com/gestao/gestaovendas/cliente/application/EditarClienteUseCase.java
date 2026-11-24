package br.com.gestao.gestaovendas.cliente.application;

import br.com.gestao.gestaovendas.cliente.domain.Cliente;
import br.com.gestao.gestaovendas.cliente.dto.ClienteEditDTO;
import br.com.gestao.gestaovendas.cliente.dto.ClienteResponseDTO;
import br.com.gestao.gestaovendas.cliente.mapper.ClienteMapper;
import br.com.gestao.gestaovendas.cliente.repository.ClienteRepository;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EditarClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Transactional
    public ClienteResponseDTO editar(Long negocioId, Long id, ClienteEditDTO request) {
        Cliente cliente = clienteRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        clienteMapper.updateEntity(cliente, request);
        CadastrarClienteUseCase.normalizar(cliente);
        return clienteMapper.toResponse(cliente);
    }
}
