package br.com.gestao.gestaovendas.troca.application;

import br.com.gestao.gestaovendas.cliente.domain.ClientesDoNegocio;
import br.com.gestao.gestaovendas.common.dto.DataHoraRegistro;
import br.com.gestao.gestaovendas.common.security.UsuarioLogado;
import br.com.gestao.gestaovendas.common.text.Textos;
import br.com.gestao.gestaovendas.estoque.domain.EventosEstoque.PerdaRegistrada;
import br.com.gestao.gestaovendas.estoque.domain.EventosEstoque.TrocaRegistrada;
import br.com.gestao.gestaovendas.estoque.domain.MovimentacaoService;
import br.com.gestao.gestaovendas.estoque.domain.MovimentacaoService.Movimento;
import br.com.gestao.gestaovendas.estoque.dto.MovimentacaoResponseDTO;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.domain.ProdutosDoNegocio;
import br.com.gestao.gestaovendas.troca.domain.DestinoDevolucao;
import br.com.gestao.gestaovendas.troca.domain.Troca;
import br.com.gestao.gestaovendas.troca.dto.TrocaRequestDTO;
import br.com.gestao.gestaovendas.troca.dto.TrocaResponseDTO;
import br.com.gestao.gestaovendas.troca.repository.TrocaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Devolvido com destino ESTOQUE: TROCA_ENTRADA (+).
 * Devolvido com destino PERDA: TROCA_ENTRADA (+) e PERDA (-): o estoque não muda,
 * mas a perda aparece nos relatórios. Entregue: TROCA_SAIDA (-).
 */
@Service
@RequiredArgsConstructor
public class RegistrarTrocaUseCase {

    private final TrocaRepository trocaRepository;
    private final ClientesDoNegocio clientesDoNegocio;
    private final ProdutosDoNegocio produtosDoNegocio;
    private final MovimentacaoService movimentacaoService;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    @Transactional
    public TrocaResponseDTO registrar(Long negocioId, TrocaRequestDTO request) {
        var cliente = clientesDoNegocio.ativo(negocioId, request.clienteId());
        List<TrocaRequestDTO.Entregue> entregues = request.entregues() == null ? List.of() : request.entregues();
        // devolvido pode ser produto já desativado; o entregue precisa estar ativo
        Map<Long, Produto> devolvidos = produtosDoNegocio.carregar(negocioId,
                request.devolvidos().stream().map(TrocaRequestDTO.Devolvido::produtoId).toList(), false);
        Map<Long, Produto> paraEntregar = entregues.isEmpty() ? Map.of() : produtosDoNegocio.carregar(negocioId,
                entregues.stream().map(TrocaRequestDTO.Entregue::produtoId).toList(), true);
        LocalDateTime dataHora = DataHoraRegistro.resolver(request.dataHora(), clock);
        Long usuarioId = UsuarioLogado.id();

        var todos = new java.util.HashSet<>(devolvidos.keySet());
        todos.addAll(paraEntregar.keySet());
        movimentacaoService.travar(todos);

        Troca troca = trocaRepository.save(
                new Troca(negocioId, cliente, dataHora, Textos.limpar(request.observacao()), usuarioId));

        List<MovimentacaoResponseDTO> movs = new ArrayList<>();
        for (var d : request.devolvidos()) {
            Produto produto = devolvidos.get(d.produtoId());
            movs.add(MovimentacaoResponseDTO.de(movimentacaoService.registrar(
                    Movimento.trocaEntrada(produto, d.quantidade(), dataHora, troca.getId(), usuarioId))));
            if (d.destino() == DestinoDevolucao.PERDA) {
                movs.add(MovimentacaoResponseDTO.de(movimentacaoService.registrar(Movimento.perda(produto,
                        d.quantidade(), dataHora, d.motivoPerda(), "Devolvido na troca", troca.getId(), usuarioId))));
                eventos.publishEvent(new PerdaRegistrada(negocioId, produto.getId(), d.quantidade(),
                        d.motivoPerda(), usuarioId));
            }
        }
        for (var e : entregues) {
            movs.add(MovimentacaoResponseDTO.de(movimentacaoService.registrar(Movimento.trocaSaida(
                    paraEntregar.get(e.produtoId()), e.quantidade(), dataHora, troca.getId(), usuarioId))));
        }
        eventos.publishEvent(new TrocaRegistrada(negocioId, troca.getId(), cliente.getId(), usuarioId));
        return TrocaResponseDTO.de(troca, movs);
    }
}
