package br.com.gestao.gestaovendas.despesa.controller;

import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.despesa.application.ConsultarDespesaUseCase;
import br.com.gestao.gestaovendas.despesa.application.EditarDespesaUseCase;
import br.com.gestao.gestaovendas.despesa.application.RegistrarDespesaUseCase;
import br.com.gestao.gestaovendas.despesa.application.RemoverDespesaUseCase;
import br.com.gestao.gestaovendas.despesa.domain.TipoDespesa;
import br.com.gestao.gestaovendas.despesa.dto.DespesaEditDTO;
import br.com.gestao.gestaovendas.despesa.dto.DespesaRequestDTO;
import br.com.gestao.gestaovendas.despesa.dto.DespesaResponseDTO;
import br.com.gestao.gestaovendas.despesa.dto.ResumoDespesasDTO;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/despesa")
public class DespesaController {

    private final RegistrarDespesaUseCase registrarDespesaUseCase;
    private final EditarDespesaUseCase editarDespesaUseCase;
    private final RemoverDespesaUseCase removerDespesaUseCase;
    private final ConsultarDespesaUseCase consultarDespesaUseCase;

    @PostMapping
    public ResponseEntity<DespesaResponseDTO> registrar(NegocioAtual negocio, @Valid @RequestBody DespesaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrarDespesaUseCase.registrar(negocio.id(), dto));
    }

    @GetMapping
    public PaginaDTO<DespesaResponseDTO> listar(
            NegocioAtual negocio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) TipoDespesa tipo,
            @RequestParam(required = false) Long veiculoId,
            @ParameterObject @PageableDefault(sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {
        return consultarDespesaUseCase.listar(negocio.id(), de, ate, tipo, veiculoId, pageable);
    }

    /** Totais do período por tipo e por veículo. */
    @GetMapping("/resumo")
    public ResumoDespesasDTO resumo(
            NegocioAtual negocio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return consultarDespesaUseCase.resumo(negocio.id(), de, ate);
    }

    @GetMapping("/{id}")
    public DespesaResponseDTO buscar(NegocioAtual negocio, @PathVariable Long id) {
        return consultarDespesaUseCase.buscar(negocio.id(), id);
    }

    @PatchMapping("/{id}")
    public DespesaResponseDTO editar(NegocioAtual negocio, @PathVariable Long id, @Valid @RequestBody DespesaEditDTO dto) {
        return editarDespesaUseCase.editar(negocio.id(), id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(NegocioAtual negocio, @PathVariable Long id) {
        removerDespesaUseCase.remover(negocio.id(), id);
        return ResponseEntity.noContent().build();
    }
}
