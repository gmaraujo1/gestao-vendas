package br.com.gestao.gestaovendas.estoque.controller;

import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.estoque.application.ListarPerdasUseCase;
import br.com.gestao.gestaovendas.estoque.application.RegistrarPerdaUseCase;
import br.com.gestao.gestaovendas.estoque.dto.MovimentacaoResponseDTO;
import br.com.gestao.gestaovendas.estoque.dto.PerdaRequestDTO;
import br.com.gestao.gestaovendas.negocio.context.NegocioAtual;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/perda")
public class PerdaController {

    private final RegistrarPerdaUseCase registrarPerdaUseCase;
    private final ListarPerdasUseCase listarPerdasUseCase;

    @PostMapping
    public ResponseEntity<MovimentacaoResponseDTO> registrar(NegocioAtual negocio,
                                                             @Valid @RequestBody PerdaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrarPerdaUseCase.registrar(negocio.id(), dto));
    }

    /** ?de=2026-11-01&ate=2026-11-30 (datas inclusivas, opcionais). */
    @GetMapping
    public PaginaDTO<MovimentacaoResponseDTO> listar(
            NegocioAtual negocio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @ParameterObject @PageableDefault(sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return listarPerdasUseCase.listar(negocio.id(), de, ate, pageable);
    }
}
