package br.com.gestao.gestaovendas.veiculo.application;

import br.com.gestao.gestaovendas.exception.ConflictException;
import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.veiculo.domain.Placas;
import br.com.gestao.gestaovendas.veiculo.domain.Veiculo;
import br.com.gestao.gestaovendas.veiculo.dto.VeiculoEditDTO;
import br.com.gestao.gestaovendas.veiculo.dto.VeiculoResponseDTO;
import br.com.gestao.gestaovendas.veiculo.mapper.VeiculoMapper;
import br.com.gestao.gestaovendas.veiculo.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EditarVeiculoUseCase {

    private final VeiculoRepository veiculoRepository;
    private final VeiculoMapper veiculoMapper;

    @Transactional
    public VeiculoResponseDTO editar(Long id, VeiculoEditDTO request) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

        if (request.descricao() != null) {
            veiculo.setDescricao(request.descricao().trim());
        }
        if (request.placa() != null) {
            String placa = Placas.normalizar(request.placa());
            if (placa != null && veiculoRepository.existsByPlacaAndIdNot(placa, id)) {
                throw new ConflictException("Já existe um veículo com essa placa");
            }
            veiculo.setPlaca(placa);
        }
        if (request.ativo() != null) {
            veiculo.setAtivo(request.ativo());
        }
        return veiculoMapper.toResponse(veiculo);
    }
}
