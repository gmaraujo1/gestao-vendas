package br.com.gestao.gestaovendas.produto.controller;

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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/produto")
public class ProdutoController {

    private final CadastrarProdutoUseCase cadastrarProdutoUseCase;
    private final ListarProdutoUseCase listarProdutoUseCase;
    private final EditarProdutoUseCase editarProdutoUseCase;
    private final RemoverProdutoUseCase removerProdutoUseCase;

    @PostMapping(path = "/cadastrar")
    public ResponseEntity<ProdutoResponseDTO> cadastrarNovoProduto(NegocioAtual negocio, @Valid @RequestBody ProdutoRequestDTO dto) {
        ProdutoResponseDTO resposta = cadastrarProdutoUseCase.cadastrar(negocio.id(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listarProdutos(
            NegocioAtual negocio,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) BigDecimal precoMinimo,
            @RequestParam(required = false) BigDecimal precoMaximo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate atualizadoDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate atualizadoAte,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate criadoDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate criadoAte
    ) {
        ProdutoFiltroDTO filtro = new ProdutoFiltroDTO(
                descricao, precoMinimo, precoMaximo, atualizadoDe, atualizadoAte, criadoDe, criadoAte
        );
        return ResponseEntity.ok(listarProdutoUseCase.listar(negocio.id(), filtro));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> editarProduto(
            NegocioAtual negocio,
            @PathVariable Long id,
            @Valid @RequestBody ProdutoRequestEditDTO dto) {
        ProdutoResponseDTO resposta = editarProdutoUseCase.atualizar(negocio.id(), id, dto);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto(NegocioAtual negocio, @PathVariable Long id) {
        removerProdutoUseCase.deletar(negocio.id(), id);
        return ResponseEntity.noContent().build();
    }
}
