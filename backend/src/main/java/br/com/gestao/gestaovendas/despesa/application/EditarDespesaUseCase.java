package br.com.gestao.gestaovendas.despesa.application;

import br.com.gestao.gestaovendas.common.text.Textos;
import br.com.gestao.gestaovendas.despesa.domain.Despesa;
import br.com.gestao.gestaovendas.despesa.domain.DespesaValidator;
import br.com.gestao.gestaovendas.despesa.dto.DespesaEditDTO;
import br.com.gestao.gestaovendas.despesa.dto.DespesaResponseDTO;
import br.com.gestao.gestaovendas.despesa.repository.DespesaRepository;
import br.com.gestao.gestaovendas.exception.RequisicaoInvalidaException;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Só ADMIN (ver SecurityConfig). */
@Service
@RequiredArgsConstructor
public class EditarDespesaUseCase {

    private final DespesaRepository despesaRepository;
    private final DespesaValidator despesaValidator;

    @Transactional
    public DespesaResponseDTO editar(Long negocioId, Long id, DespesaEditDTO request) {
        Despesa despesa = despesaRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada"));
        if (Boolean.TRUE.equals(request.semVeiculo()) && request.veiculoId() != null) {
            throw new RequisicaoInvalidaException("Informe veiculoId ou semVeiculo, não os dois");
        }
        if (request.data() != null) {
            despesa.setData(despesaValidator.data(request.data()));
        }
        if (request.valor() != null) {
            despesa.setValor(request.valor());
        }
        if (request.tipo() != null) {
            despesa.setTipo(request.tipo());
        }
        if (request.descricao() != null) {
            despesa.setDescricao(Textos.limpar(request.descricao()));
        }
        if (request.veiculoId() != null) {
            despesa.setVeiculo(despesaValidator.veiculoAtivo(request.veiculoId()));
        } else if (Boolean.TRUE.equals(request.semVeiculo())) {
            despesa.setVeiculo(null);
        }
        return DespesaResponseDTO.de(despesa);
    }
}
