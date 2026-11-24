package br.com.gestao.gestaovendas.venda.application;

import br.com.gestao.gestaovendas.common.security.UsuarioLogado;
import br.com.gestao.gestaovendas.estoque.domain.EventosEstoque.VendaCancelada;
import br.com.gestao.gestaovendas.estoque.domain.MovimentacaoService;
import br.com.gestao.gestaovendas.estoque.domain.MovimentacaoService.Movimento;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.venda.domain.Venda;
import br.com.gestao.gestaovendas.venda.domain.VendaItem;
import br.com.gestao.gestaovendas.venda.dto.CancelamentoRequestDTO;
import br.com.gestao.gestaovendas.venda.dto.VendaResponseDTO;
import br.com.gestao.gestaovendas.venda.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/** Cancela a venda (só ADMIN) e devolve os itens ao estoque com VENDA_ESTORNO. */
@Service
@RequiredArgsConstructor
public class CancelarVendaUseCase {

    private final VendaRepository vendaRepository;
    private final MovimentacaoService movimentacaoService;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    @Transactional
    public VendaResponseDTO cancelar(Long negocioId, Long vendaId, CancelamentoRequestDTO request) {
        Venda venda = vendaRepository.travarParaCancelar(vendaId, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada"));
        Long usuarioId = UsuarioLogado.id();
        LocalDateTime agora = LocalDateTime.now(clock);
        String motivo = request.motivo().trim();

        venda.cancelar(motivo, usuarioId, agora);
        movimentacaoService.travar(venda.getItens().stream().map(i -> i.getProduto().getId()).toList());
        for (VendaItem item : venda.getItens()) {
            movimentacaoService.registrar(Movimento.estornoVenda(item.getProduto(), item.getQuantidade(), agora,
                    venda.getId(), motivo, usuarioId, item.getCustoUnitario()));
        }
        eventos.publishEvent(new VendaCancelada(negocioId, venda.getId(), motivo, usuarioId));
        return VendaResponseDTO.de(venda);
    }
}
