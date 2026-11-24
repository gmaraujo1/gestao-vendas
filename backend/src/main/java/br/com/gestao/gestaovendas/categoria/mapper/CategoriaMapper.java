package br.com.gestao.gestaovendas.categoria.mapper;

import br.com.gestao.gestaovendas.categoria.domain.Categoria;
import br.com.gestao.gestaovendas.categoria.dto.CategoriaResponseDTO;
import br.com.gestao.gestaovendas.common.mapper.MapStructConfig;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface CategoriaMapper {

    CategoriaResponseDTO toResponse(Categoria categoria);
}
