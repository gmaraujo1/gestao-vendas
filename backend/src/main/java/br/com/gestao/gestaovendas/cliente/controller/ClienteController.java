package br.com.gestao.gestaovendas.cliente.controller;

import br.com.gestao.gestaovendas.cliente.application.BuscarClienteUseCase;
import br.com.gestao.gestaovendas.cliente.application.CadastrarClienteUseCase;
import br.com.gestao.gestaovendas.cliente.application.EditarClienteUseCase;
import br.com.gestao.gestaovendas.cliente.application.ListarClienteUseCase;
import br.com.gestao.gestaovendas.cliente.application.RemoverClienteUseCase;
import br.com.gestao.gestaovendas.cliente.dto.ClienteEditDTO;
import br.com.gestao.gestaovendas.cliente.dto.ClienteRequestDTO;
import br.com.gestao.gestaovendas.cliente.dto.ClienteResponseDTO;
import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.negocio.context.NegocioAtual;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cliente")
public class ClienteController {

    private final CadastrarClienteUseCase cadastrarClienteUseCase;
    private final ListarClienteUseCase listarClienteUseCase;
    private final BuscarClienteUseCase buscarClienteUseCase;
    private final EditarClienteUseCase editarClienteUseCase;
    private final RemoverClienteUseCase removerClienteUseCase;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> cadastrar(NegocioAtual negocio, @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrarClienteUseCase.cadastrar(negocio.id(), dto));
    }

    /** Paginado; ?nome= busca por parte do nome. */
    @GetMapping
    public PaginaDTO<ClienteResponseDTO> listar(
            NegocioAtual negocio,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo,
            @ParameterObject @PageableDefault(sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return listarClienteUseCase.listar(negocio.id(), nome, ativo, pageable);
    }

    @GetMapping("/{id}")
    public ClienteResponseDTO buscar(NegocioAtual negocio, @PathVariable Long id) {
        return buscarClienteUseCase.buscar(negocio.id(), id);
    }

    @PatchMapping("/{id}")
    public ClienteResponseDTO editar(NegocioAtual negocio, @PathVariable Long id,
                                     @Valid @RequestBody ClienteEditDTO dto) {
        return editarClienteUseCase.editar(negocio.id(), id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(NegocioAtual negocio, @PathVariable Long id) {
        removerClienteUseCase.remover(negocio.id(), id);
        return ResponseEntity.noContent().build();
    }
}
