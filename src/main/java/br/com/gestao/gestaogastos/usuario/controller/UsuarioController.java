package br.com.gestao.gestaogastos.usuario.controller;

import br.com.gestao.gestaogastos.usuario.application.CriarUsuarioUseCase;
import br.com.gestao.gestaogastos.usuario.application.EditarUsuarioUseCase;
import br.com.gestao.gestaogastos.usuario.dto.UsuarioRequestDTO;
import br.com.gestao.gestaogastos.usuario.dto.UsuarioRequestEditDTO;
import br.com.gestao.gestaogastos.usuario.dto.UsuarioResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuario")
public class UsuarioController {
    private final CriarUsuarioUseCase criarUsuarioUseCase;
    private final EditarUsuarioUseCase editarUsuarioUseCase;

    // endpoint para criacao de novo usuário
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

}
