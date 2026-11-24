package br.com.gestao.gestaovendas.produto.controller;

import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.negocio.context.NegocioAtual;
import br.com.gestao.gestaovendas.produto.application.CadastrarProdutoUseCase;
import br.com.gestao.gestaovendas.produto.application.EditarProdutoUseCase;
import br.com.gestao.gestaovendas.produto.application.ListarProdutoUseCase;
import br.com.gestao.gestaovendas.produto.application.RemoverProdutoUseCase;
import br.com.gestao.gestaovendas.produto.dto.ProdutoFiltroDTO;
import br.com.gestao.gestaovendas.produto.dto.ProdutoRequestDTO;
import br.com.gestao.gestaovendas.produto.dto.ProdutoRequestEditDTO;
import br.com.gestao.gestaovendas.produto.dto.ProdutoResponseDTO;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/produto")
public class ProdutoController {

    private final CadastrarProdutoUseCase cadastrarProdutoUseCase;
    private final ListarProdutoUseCase listarProdutoUseCase;
    private final EditarProdutoUseCase editarProdutoUseCase;
    private final RemoverProdutoUseCase removerProdutoUseCase;

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> cadastrar(NegocioAtual negocio, @Valid @RequestBody ProdutoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrarProdutoUseCase.cadastrar(negocio.id(), dto));
    }

    /** Paginado: ?page=0&size=20&sort=descricao,asc */
    @GetMapping
    public PaginaDTO<ProdutoResponseDTO> listar(
            NegocioAtual negocio,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) BigDecimal precoMinimo,
            @RequestParam(required = false) BigDecimal precoMaximo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate atualizadoDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate atualizadoAte,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate criadoDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate criadoAte,
            @ParameterObject @PageableDefault(sort = "descricao", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var filtro = new ProdutoFiltroDTO(descricao, categoriaId, ativo, precoMinimo, precoMaximo,
                atualizadoDe, atualizadoAte, criadoDe, criadoAte);
        return listarProdutoUseCase.listar(negocio.id(), filtro, pageable);
    }

    @PatchMapping("/{id}")
    public ProdutoResponseDTO editar(NegocioAtual negocio, @PathVariable Long id,
                                     @Valid @RequestBody ProdutoRequestEditDTO dto) {
        return editarProdutoUseCase.atualizar(negocio.id(), id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(NegocioAtual negocio, @PathVariable Long id) {
        removerProdutoUseCase.deletar(negocio.id(), id);
        return ResponseEntity.noContent().build();
    }
}
