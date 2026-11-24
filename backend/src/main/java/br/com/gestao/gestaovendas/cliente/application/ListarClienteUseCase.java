package br.com.gestao.gestaovendas.cliente.application;

import br.com.gestao.gestaovendas.cliente.dto.ClienteResponseDTO;
import br.com.gestao.gestaovendas.cliente.mapper.ClienteMapper;
import br.com.gestao.gestaovendas.cliente.repository.ClienteRepository;
import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.common.dto.Paginas;
import br.com.gestao.gestaovendas.common.text.Like;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListarClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    private static final Set<String> ORDENACAO = Set.of("nome", "cidade", "criadoEm", "id");

    @Transactional(readOnly = true)
    public PaginaDTO<ClienteResponseDTO> listar(Long negocioId, String nome, Boolean ativo, Pageable pageable) {
        String termo = (nome == null || nome.isBlank()) ? null : Like.escapar(nome.trim());
        return PaginaDTO.de(clienteRepository.buscar(negocioId, termo, ativo, Paginas.validar(pageable, ORDENACAO)), clienteMapper::toResponse);
    }
}
