package br.com.gestao.gestaovendas.veiculo.controller;

import br.com.gestao.gestaovendas.veiculo.application.CadastrarVeiculoUseCase;
import br.com.gestao.gestaovendas.veiculo.application.EditarVeiculoUseCase;
import br.com.gestao.gestaovendas.veiculo.application.ListarVeiculoUseCase;
import br.com.gestao.gestaovendas.veiculo.application.RemoverVeiculoUseCase;
import br.com.gestao.gestaovendas.veiculo.dto.VeiculoEditDTO;
import br.com.gestao.gestaovendas.veiculo.dto.VeiculoRequestDTO;
import br.com.gestao.gestaovendas.veiculo.dto.VeiculoResponseDTO;
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

/** Global (sem X-Negocio-Id). Leitura para todos; alteração só ADMIN (ver SecurityConfig). */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/veiculo")
public class VeiculoController {

    private final CadastrarVeiculoUseCase cadastrarVeiculoUseCase;
    private final ListarVeiculoUseCase listarVeiculoUseCase;
    private final EditarVeiculoUseCase editarVeiculoUseCase;
    private final RemoverVeiculoUseCase removerVeiculoUseCase;

    @PostMapping
    public ResponseEntity<VeiculoResponseDTO> cadastrar(@Valid @RequestBody VeiculoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastrarVeiculoUseCase.cadastrar(dto));
    }

    @GetMapping
    public List<VeiculoResponseDTO> listar(@RequestParam(defaultValue = "false") boolean apenasAtivos) {
        return listarVeiculoUseCase.listar(apenasAtivos);
    }

    @PatchMapping("/{id}")
    public VeiculoResponseDTO editar(@PathVariable Long id, @Valid @RequestBody VeiculoEditDTO dto) {
        return editarVeiculoUseCase.editar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        removerVeiculoUseCase.remover(id);
        return ResponseEntity.noContent().build();
    }
}
