package br.com.gestao.gestaovendas.veiculo.application;

import br.com.gestao.gestaovendas.exception.ConflictException;
import br.com.gestao.gestaovendas.veiculo.domain.Placas;
import br.com.gestao.gestaovendas.veiculo.domain.Veiculo;
import br.com.gestao.gestaovendas.veiculo.dto.VeiculoRequestDTO;
import br.com.gestao.gestaovendas.veiculo.dto.VeiculoResponseDTO;
import br.com.gestao.gestaovendas.veiculo.mapper.VeiculoMapper;
import br.com.gestao.gestaovendas.veiculo.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CadastrarVeiculoUseCase {

    private final VeiculoRepository veiculoRepository;
    private final VeiculoMapper veiculoMapper;

    @Transactional
    public VeiculoResponseDTO cadastrar(VeiculoRequestDTO request) {
        String placa = Placas.normalizar(request.placa());
        if (placa != null && veiculoRepository.existsByPlaca(placa)) {
            throw new ConflictException("Já existe um veículo com essa placa");
        }
        Veiculo veiculo = new Veiculo();
        veiculo.setDescricao(request.descricao().trim());
        veiculo.setPlaca(placa);
        return veiculoMapper.toResponse(veiculoRepository.save(veiculo));
    }
}
