package br.com.gestao.gestaovendas.estoque.application;

import br.com.gestao.gestaovendas.common.dto.DataHoraRegistro;
import br.com.gestao.gestaovendas.common.security.UsuarioLogado;
import br.com.gestao.gestaovendas.common.text.Textos;
import br.com.gestao.gestaovendas.estoque.domain.EventosEstoque.PerdaRegistrada;
import br.com.gestao.gestaovendas.estoque.domain.MovimentacaoService;
import br.com.gestao.gestaovendas.estoque.domain.MovimentacaoService.Movimento;
import br.com.gestao.gestaovendas.estoque.dto.MovimentacaoResponseDTO;
import br.com.gestao.gestaovendas.estoque.dto.PerdaRequestDTO;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.domain.ProdutosDoNegocio;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class RegistrarPerdaUseCase {

    private final ProdutosDoNegocio produtosDoNegocio;
    private final MovimentacaoService movimentacaoService;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    /** Produto inativo também pode ter perda (descartar o que sobrou). */
    @Transactional
    public MovimentacaoResponseDTO registrar(Long negocioId, PerdaRequestDTO request) {
        Produto produto = produtosDoNegocio.carregar(negocioId, request.produtoId(), false);
        Long usuarioId = UsuarioLogado.id();
        movimentacaoService.travar(java.util.List.of(produto.getId()));
        var mov = movimentacaoService.registrar(Movimento.perda(produto, request.quantidade(),
                DataHoraRegistro.resolver(request.dataHora(), clock), request.motivo(),
                Textos.limpar(request.observacao()), null, usuarioId));
        eventos.publishEvent(new PerdaRegistrada(negocioId, produto.getId(), request.quantidade(),
                request.motivo(), usuarioId));
        return MovimentacaoResponseDTO.de(mov);
    }
}
