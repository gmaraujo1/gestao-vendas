package br.com.gestao.gestaovendas.veiculo.application;

import br.com.gestao.gestaovendas.veiculo.dto.VeiculoResponseDTO;
import br.com.gestao.gestaovendas.veiculo.mapper.VeiculoMapper;
import br.com.gestao.gestaovendas.veiculo.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarVeiculoUseCase {

    private final VeiculoRepository veiculoRepository;
    private final VeiculoMapper veiculoMapper;

    @Transactional(readOnly = true)
    public List<VeiculoResponseDTO> listar(boolean apenasAtivos) {
        var veiculos = apenasAtivos
                ? veiculoRepository.findAllByAtivoTrueOrderByDescricaoAsc()
                : veiculoRepository.findAllByOrderByDescricaoAsc();
        return veiculos.stream().map(veiculoMapper::toResponse).toList();
    }
}
