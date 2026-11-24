package br.com.gestao.gestaovendas.cliente.mapper;

import br.com.gestao.gestaovendas.cliente.domain.Cliente;
import br.com.gestao.gestaovendas.cliente.dto.ClienteEditDTO;
import br.com.gestao.gestaovendas.cliente.dto.ClienteRequestDTO;
import br.com.gestao.gestaovendas.cliente.dto.ClienteResponseDTO;
import br.com.gestao.gestaovendas.common.mapper.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface ClienteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "negocioId", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    Cliente toEntity(ClienteRequestDTO dto);

    ClienteResponseDTO toResponse(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "negocioId", ignore = true)
    void updateEntity(@MappingTarget Cliente cliente, ClienteEditDTO dto);
}
