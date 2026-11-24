package br.com.gestao.gestaovendas.veiculo.mapper;

import br.com.gestao.gestaovendas.common.mapper.MapStructConfig;
import br.com.gestao.gestaovendas.veiculo.domain.Veiculo;
import br.com.gestao.gestaovendas.veiculo.dto.VeiculoResponseDTO;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface VeiculoMapper {

    VeiculoResponseDTO toResponse(Veiculo veiculo);
}
