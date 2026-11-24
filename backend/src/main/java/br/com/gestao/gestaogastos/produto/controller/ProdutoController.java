package br.com.gestao.gestaogastos.produto.controller;

import br.com.gestao.gestaogastos.produto.application.CadastrarProdutoUseCase;
import br.com.gestao.gestaogastos.produto.application.EditarProdutoUseCase;
import br.com.gestao.gestaogastos.produto.application.ListarProdutoUseCase;
import br.com.gestao.gestaogastos.produto.application.RemoverProdutoUseCase;
import br.com.gestao.gestaogastos.produto.dto.ProdutoFiltroDTO;
import br.com.gestao.gestaogastos.produto.dto.ProdutoRequestDTO;
import br.com.gestao.gestaogastos.produto.dto.ProdutoRequestEditDTO;
import br.com.gestao.gestaogastos.produto.dto.ProdutoResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ProdutoResponseDTO> cadastrarNovoProduto(@Valid @RequestBody ProdutoRequestDTO dto) {
        ProdutoResponseDTO resposta = cadastrarProdutoUseCase.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listarProdutos(
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) Double precoMinimo,
            @RequestParam(required = false) Double precoMaximo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate atualizadoDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate atualizadoAte,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate criadoDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate criadoAte
    ) {
        ProdutoFiltroDTO filtro = new ProdutoFiltroDTO(
                descricao, precoMinimo, precoMaximo, atualizadoDe, atualizadoAte, criadoDe, criadoAte
        );
        return ResponseEntity.ok(listarProdutoUseCase.listar(filtro));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> editarProduto(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoRequestEditDTO dto) {
        ProdutoResponseDTO resposta = editarProdutoUseCase.atualizar(id, dto);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
        removerProdutoUseCase.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
