package br.com.gestao.gestaovendas.estoque.controller;

import br.com.gestao.gestaovendas.common.dto.PaginaDTO;
import br.com.gestao.gestaovendas.estoque.application.AjustarEstoqueUseCase;
import br.com.gestao.gestaovendas.estoque.application.ConsultarEstoqueUseCase;
import br.com.gestao.gestaovendas.estoque.dto.AjusteRequestDTO;
import br.com.gestao.gestaovendas.estoque.dto.AjusteResponseDTO;
import br.com.gestao.gestaovendas.estoque.dto.ExtratoProdutoDTO;
import br.com.gestao.gestaovendas.estoque.dto.SaldoProdutoDTO;
import br.com.gestao.gestaovendas.negocio.context.NegocioAtual;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/estoque")
public class EstoqueController {

    private final ConsultarEstoqueUseCase consultarEstoqueUseCase;
    private final AjustarEstoqueUseCase ajustarEstoqueUseCase;

    /** Saldo de cada produto (soma das movimentações; pode ser negativo). */
    @GetMapping
    public PaginaDTO<SaldoProdutoDTO> saldos(
            NegocioAtual negocio,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String descricao,
            @ParameterObject @PageableDefault(sort = "descricao", direction = Sort.Direction.ASC) Pageable pageable) {
        return consultarEstoqueUseCase.saldos(negocio.id(), categoriaId, ativo, descricao, pageable);
    }

    /** Saldo e histórico de movimentações de um produto (mais recentes primeiro). */
    @GetMapping("/produto/{produtoId}")
    public ExtratoProdutoDTO extrato(
            NegocioAtual negocio,
            @PathVariable Long produtoId,
            @ParameterObject @PageableDefault(sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return consultarEstoqueUseCase.extrato(negocio.id(), produtoId, pageable);
    }

    /** "Informar estoque" (só ADMIN). */
    @PostMapping("/ajuste")
    public AjusteResponseDTO ajustar(NegocioAtual negocio, @Valid @RequestBody AjusteRequestDTO dto) {
        return ajustarEstoqueUseCase.ajustar(negocio.id(), dto);
    }
}
