package br.com.gestao.gestaovendas.estoque.application;

import br.com.gestao.gestaovendas.common.security.UsuarioLogado;
import br.com.gestao.gestaovendas.common.text.Textos;
import br.com.gestao.gestaovendas.estoque.domain.EventosEstoque.EstoqueAjustado;
import br.com.gestao.gestaovendas.estoque.domain.MovimentacaoEstoque;
import br.com.gestao.gestaovendas.estoque.domain.MovimentacaoService;
import br.com.gestao.gestaovendas.estoque.domain.MovimentacaoService.Movimento;
import br.com.gestao.gestaovendas.estoque.dto.AjusteRequestDTO;
import br.com.gestao.gestaovendas.estoque.dto.AjusteResponseDTO;
import br.com.gestao.gestaovendas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.domain.ProdutosDoNegocio;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** "Informar estoque": grava como AJUSTE a diferença entre a contagem real e o saldo. */
@Service
@RequiredArgsConstructor
public class AjustarEstoqueUseCase {

    private final ProdutosDoNegocio produtosDoNegocio;
    private final MovimentacaoEstoqueRepository repository;
    private final MovimentacaoService movimentacaoService;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    @Transactional
    public AjusteResponseDTO ajustar(Long negocioId, AjusteRequestDTO request) {
        Produto produto = produtosDoNegocio.carregar(negocioId, request.produtoId(), false);
        movimentacaoService.travar(List.of(produto.getId()));
        BigDecimal saldoAnterior = repository.saldo(negocioId, produto.getId());
        BigDecimal diferenca = request.quantidadeContada().subtract(saldoAnterior);

        if (diferenca.signum() == 0) {
            return new AjusteResponseDTO(produto.getId(), saldoAnterior, saldoAnterior, BigDecimal.ZERO, null);
        }
        Long usuarioId = UsuarioLogado.id();
        MovimentacaoEstoque mov = movimentacaoService.registrar(Movimento.ajuste(produto, diferenca,
                LocalDateTime.now(clock), Textos.limpar(request.observacao()), usuarioId));
        eventos.publishEvent(new EstoqueAjustado(negocioId, produto.getId(), diferenca, usuarioId));
        return new AjusteResponseDTO(produto.getId(), saldoAnterior, request.quantidadeContada(), diferenca, mov.getId());
    }
}
