package br.com.gestao.gestaovendas.produto.mapper;

import br.com.gestao.gestaovendas.common.mapper.MapStructConfig;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.dto.ProdutoRequestDTO;
import br.com.gestao.gestaovendas.produto.dto.ProdutoRequestEditDTO;
import br.com.gestao.gestaovendas.produto.dto.ProdutoResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface ProdutoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "negocioId", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    Produto toEntity(ProdutoRequestDTO dto);

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNome", source = "categoria.nome")
    ProdutoResponseDTO toResponse(Produto produto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "negocioId", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    void updateEntity(@MappingTarget Produto produto, ProdutoRequestEditDTO dto);
}
