package br.com.gestao.gestaovendas.venda.application;

import br.com.gestao.gestaovendas.cliente.domain.ClientesDoNegocio;
import br.com.gestao.gestaovendas.common.dto.DataHoraRegistro;
import br.com.gestao.gestaovendas.common.security.UsuarioLogado;
import br.com.gestao.gestaovendas.common.text.Textos;
import br.com.gestao.gestaovendas.estoque.domain.EventosEstoque.VendaRegistrada;
import br.com.gestao.gestaovendas.estoque.domain.MovimentacaoService;
import br.com.gestao.gestaovendas.estoque.domain.MovimentacaoService.Movimento;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.domain.ProdutosDoNegocio;
import br.com.gestao.gestaovendas.venda.domain.Venda;
import br.com.gestao.gestaovendas.venda.domain.VendaItem;
import br.com.gestao.gestaovendas.venda.dto.VendaRequestDTO;
import br.com.gestao.gestaovendas.venda.dto.VendaResponseDTO;
import br.com.gestao.gestaovendas.venda.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

/** Registra a venda e dá saída de cada item no estoque (pode ficar negativo). */
@Service
@RequiredArgsConstructor
public class RegistrarVendaUseCase {

    private final VendaRepository vendaRepository;
    private final ClientesDoNegocio clientesDoNegocio;
    private final ProdutosDoNegocio produtosDoNegocio;
    private final MovimentacaoService movimentacaoService;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    @Transactional
    public VendaResponseDTO registrar(Long negocioId, VendaRequestDTO request) {
        var cliente = clientesDoNegocio.ativo(negocioId, request.clienteId());
        Map<Long, Produto> produtos = produtosDoNegocio.carregar(negocioId,
                request.itens().stream().map(VendaRequestDTO.Item::produtoId).toList(), true);
        LocalDateTime dataHora = DataHoraRegistro.resolver(request.dataHora(), clock);
        Long usuarioId = UsuarioLogado.id();

        Venda venda = new Venda(negocioId, cliente, dataHora, Textos.limpar(request.observacao()), usuarioId);
        for (var item : request.itens()) {
            venda.adicionarItem(new VendaItem(produtos.get(item.produtoId()), item.quantidade(), item.precoUnitario()));
        }
        vendaRepository.save(venda);
        movimentacaoService.travar(produtos.keySet());

        for (VendaItem item : venda.getItens()) {
            movimentacaoService.registrar(Movimento.venda(item.getProduto(), item.getQuantidade(), dataHora,
                    venda.getId(), usuarioId));
        }
        eventos.publishEvent(new VendaRegistrada(negocioId, venda.getId(), cliente.getId(), venda.getTotal(), usuarioId));
        return VendaResponseDTO.de(venda);
    }
}
