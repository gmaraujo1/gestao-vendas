package br.com.gestao.gestaovendas.categoria.controller;

import br.com.gestao.gestaovendas.categoria.application.CadastrarCategoriaUseCase;
import br.com.gestao.gestaovendas.categoria.application.EditarCategoriaUseCase;
import br.com.gestao.gestaovendas.categoria.application.ListarCategoriaUseCase;
import br.com.gestao.gestaovendas.categoria.application.RemoverCategoriaUseCase;
import br.com.gestao.gestaovendas.categoria.dto.CategoriaEditDTO;
import br.com.gestao.gestaovendas.categoria.dto.CategoriaRequestDTO;
import br.com.gestao.gestaovendas.categoria.dto.CategoriaResponseDTO;
import br.com.gestao.gestaovendas.negocio.context.NegocioAtual;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categoria")
public class CategoriaController {

    private final CadastrarCategoriaUseCase cadastrarCategoriaUseCase;
    private final ListarCategoriaUseCase listarCategoriaUseCase;
    private final EditarCategoriaUseCase editarCategoriaUseCase;
    private final RemoverCategoriaUseCase removerCategoriaUseCase;

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> cadastrar(NegocioAtual negocio,
                                                          @Valid @RequestBody CategoriaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrarCategoriaUseCase.cadastrar(negocio.id(), dto));
    }

    @GetMapping
    public List<CategoriaResponseDTO> listar(NegocioAtual negocio,
                                             @RequestParam(defaultValue = "false") boolean apenasAtivas) {
        return listarCategoriaUseCase.listar(negocio.id(), apenasAtivas);
    }

    @PatchMapping("/{id}")
    public CategoriaResponseDTO editar(NegocioAtual negocio, @PathVariable Long id,
                                       @Valid @RequestBody CategoriaEditDTO dto) {
        return editarCategoriaUseCase.editar(negocio.id(), id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(NegocioAtual negocio, @PathVariable Long id) {
        removerCategoriaUseCase.remover(negocio.id(), id);
        return ResponseEntity.noContent().build();
    }
}
