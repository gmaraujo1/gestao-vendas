package br.com.gestao.gestaovendas.usuario.controller;

import br.com.gestao.gestaovendas.usuario.application.CriarUsuarioUseCase;
import br.com.gestao.gestaovendas.usuario.application.EditarUsuarioUseCase;
import br.com.gestao.gestaovendas.usuario.application.ListarUsuarioUseCase;
import br.com.gestao.gestaovendas.usuario.application.RemoverUsuarioUseCase;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioRequestDTO;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioRequestEditDTO;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuario")
public class UsuarioController {
    private final CriarUsuarioUseCase criarUsuarioUseCase;
    private final EditarUsuarioUseCase editarUsuarioUseCase;
    private final RemoverUsuarioUseCase removerUsuarioUseCase;
    private final ListarUsuarioUseCase listarUsuarioUseCase;

    @GetMapping("/listar")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        List<UsuarioResponseDTO> usuarios = listarUsuarioUseCase.listar();
        return ResponseEntity.status(HttpStatus.OK).body(usuarios);
    }

    @PostMapping("/criar")
    public ResponseEntity<UsuarioResponseDTO> criarNovoUsuario(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO resposta = criarUsuarioUseCase.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PatchMapping("/{id}/editar")
    public ResponseEntity<UsuarioResponseDTO> editarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestEditDTO dto) {
        UsuarioResponseDTO resposta = editarUsuarioUseCase.atualizar(id, dto);
        return ResponseEntity.ok(resposta);

    }

    @DeleteMapping("/{id}/remover")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        removerUsuarioUseCase.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
