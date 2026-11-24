package br.com.gestao.gestaovendas.auth.controller;

import br.com.gestao.gestaovendas.auth.application.LoginUseCase;
import br.com.gestao.gestaovendas.auth.dto.LoginRequestDTO;
import br.com.gestao.gestaovendas.auth.dto.LoginResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(loginUseCase.login(dto));
    }
}
