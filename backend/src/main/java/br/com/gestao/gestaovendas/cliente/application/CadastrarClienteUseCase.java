package br.com.gestao.gestaovendas.cliente.application;

import br.com.gestao.gestaovendas.cliente.domain.Cliente;
import br.com.gestao.gestaovendas.common.text.Textos;
import br.com.gestao.gestaovendas.cliente.dto.ClienteRequestDTO;
import br.com.gestao.gestaovendas.cliente.dto.ClienteResponseDTO;
import br.com.gestao.gestaovendas.cliente.mapper.ClienteMapper;
import br.com.gestao.gestaovendas.cliente.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CadastrarClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    /** Nomes repetidos são permitidos (pode haver dois "João"); a observação ajuda a diferenciar. */
    @Transactional
    public ClienteResponseDTO cadastrar(Long negocioId, ClienteRequestDTO request) {
        Cliente cliente = clienteMapper.toEntity(request);
        normalizar(cliente);
        cliente.setNegocioId(negocioId);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    /** Tira espaços; texto vazio vira null (no PATCH, mandar "" limpa o campo). */
    static void normalizar(Cliente cliente) {
        cliente.setNome(cliente.getNome().trim());
        cliente.setTelefone(Textos.limpar(cliente.getTelefone()));
        cliente.setEndereco(Textos.limpar(cliente.getEndereco()));
        cliente.setCidade(Textos.limpar(cliente.getCidade()));
        cliente.setObservacao(Textos.limpar(cliente.getObservacao()));
    }
}
