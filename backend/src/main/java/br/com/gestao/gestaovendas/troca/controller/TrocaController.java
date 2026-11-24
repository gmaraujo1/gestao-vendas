package br.com.gestao.gestaovendas.troca.controller;

import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.negocio.context.NegocioAtual;
import br.com.gestao.gestaovendas.troca.application.ConsultarTrocaUseCase;
import br.com.gestao.gestaovendas.troca.application.RegistrarTrocaUseCase;
import br.com.gestao.gestaovendas.troca.dto.TrocaRequestDTO;
import br.com.gestao.gestaovendas.troca.dto.TrocaResponseDTO;
import br.com.gestao.gestaovendas.troca.dto.TrocaResumoDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/troca")
public class TrocaController {

    private final RegistrarTrocaUseCase registrarTrocaUseCase;
    private final ConsultarTrocaUseCase consultarTrocaUseCase;

    @PostMapping
    public ResponseEntity<TrocaResponseDTO> registrar(NegocioAtual negocio, @Valid @RequestBody TrocaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrarTrocaUseCase.registrar(negocio.id(), dto));
    }

    @GetMapping
    public PaginaDTO<TrocaResumoDTO> listar(
            NegocioAtual negocio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) Long clienteId,
            @ParameterObject @PageableDefault(sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return consultarTrocaUseCase.listar(negocio.id(), de, ate, clienteId, pageable);
    }

    @GetMapping("/{id}")
    public TrocaResponseDTO buscar(NegocioAtual negocio, @PathVariable Long id) {
        return consultarTrocaUseCase.buscar(negocio.id(), id);
    }
}
