package br.com.gestao.gestaovendas.despesa.application;

import br.com.gestao.gestaovendas.common.security.UsuarioLogado;
import br.com.gestao.gestaovendas.common.text.Textos;
import br.com.gestao.gestaovendas.despesa.domain.Despesa;
import br.com.gestao.gestaovendas.despesa.domain.DespesaValidator;
import br.com.gestao.gestaovendas.despesa.dto.DespesaRequestDTO;
import br.com.gestao.gestaovendas.despesa.dto.DespesaResponseDTO;
import br.com.gestao.gestaovendas.despesa.repository.DespesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrarDespesaUseCase {

    private final DespesaRepository despesaRepository;
    private final DespesaValidator despesaValidator;

    @Transactional
    public DespesaResponseDTO registrar(Long negocioId, DespesaRequestDTO request) {
        Despesa despesa = new Despesa();
        despesa.setNegocioId(negocioId);
        despesa.setData(despesaValidator.data(request.data()));
        despesa.setValor(request.valor());
        despesa.setTipo(request.tipo());
        despesa.setDescricao(Textos.limpar(request.descricao()));
        if (request.veiculoId() != null) {
            despesa.setVeiculo(despesaValidator.veiculoAtivo(request.veiculoId()));
        }
        despesa.setUsuarioId(UsuarioLogado.id());
        return DespesaResponseDTO.de(despesaRepository.save(despesa));
    }
}
