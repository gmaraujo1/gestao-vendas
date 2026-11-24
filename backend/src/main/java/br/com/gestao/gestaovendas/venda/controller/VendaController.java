package br.com.gestao.gestaovendas.venda.controller;

import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.negocio.context.NegocioAtual;
import br.com.gestao.gestaovendas.venda.application.CancelarVendaUseCase;
import br.com.gestao.gestaovendas.venda.application.ConsultarVendaUseCase;
import br.com.gestao.gestaovendas.venda.application.RegistrarVendaUseCase;
import br.com.gestao.gestaovendas.venda.domain.StatusVenda;
import br.com.gestao.gestaovendas.venda.dto.CancelamentoRequestDTO;
import br.com.gestao.gestaovendas.venda.dto.VendaRequestDTO;
import br.com.gestao.gestaovendas.venda.dto.VendaResponseDTO;
import br.com.gestao.gestaovendas.venda.dto.VendaResumoDTO;
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
@RequestMapping("/api/venda")
public class VendaController {

    private final RegistrarVendaUseCase registrarVendaUseCase;
    private final CancelarVendaUseCase cancelarVendaUseCase;
    private final ConsultarVendaUseCase consultarVendaUseCase;

    @PostMapping
    public ResponseEntity<VendaResponseDTO> registrar(NegocioAtual negocio, @Valid @RequestBody VendaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrarVendaUseCase.registrar(negocio.id(), dto));
    }

    @GetMapping
    public PaginaDTO<VendaResumoDTO> listar(
            NegocioAtual negocio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) StatusVenda status,
            @ParameterObject @PageableDefault(sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return consultarVendaUseCase.listar(negocio.id(), de, ate, clienteId, status, pageable);
    }

    @GetMapping("/{id}")
    public VendaResponseDTO buscar(NegocioAtual negocio, @PathVariable Long id) {
        return consultarVendaUseCase.buscar(negocio.id(), id);
    }

    /** Só ADMIN. Devolve os itens ao estoque; a venda continua no histórico como CANCELADA. */
    @PostMapping("/{id}/cancelar")
    public VendaResponseDTO cancelar(NegocioAtual negocio, @PathVariable Long id,
                                     @Valid @RequestBody CancelamentoRequestDTO dto) {
        return cancelarVendaUseCase.cancelar(negocio.id(), id, dto);
    }
}
