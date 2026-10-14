package br.com.gestao.gestaogastos.produto.mapper;

import br.com.gestao.gestaogastos.produto.domain.Produto;
import br.com.gestao.gestaogastos.produto.dto.ProdutoRequestDTO;
import br.com.gestao.gestaogastos.produto.dto.ProdutoRequestEditDTO;
import br.com.gestao.gestaogastos.produto.dto.ProdutoResponseDTO;
import br.com.gestao.gestaogastos.common.mapper.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface ProdutoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    Produto toEntity(ProdutoRequestDTO dto);

    ProdutoResponseDTO toResponse(Produto produto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    void updateEntity(@MappingTarget Produto produto, ProdutoRequestEditDTO dto);
}
