package br.com.gestao.gestaovendas.usuario.mapper;

import br.com.gestao.gestaovendas.negocio.domain.Negocio;
import br.com.gestao.gestaovendas.usuario.domain.Usuario;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioRequestDTO;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioRequestEditDTO;
import br.com.gestao.gestaovendas.usuario.dto.UsuarioResponseDTO;
import br.com.gestao.gestaovendas.common.mapper.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;
import java.util.TreeSet;

@Mapper(config = MapStructConfig.class)
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    @Mapping(target = "negocios", ignore = true)
    Usuario toEntity(UsuarioRequestDTO dto);

    @Mapping(target = "negocioIds", source = "negocios")
    UsuarioResponseDTO toResponse(Usuario usuario);

    default Set<Long> negocioIds(Set<Negocio> negocios) {
        Set<Long> ids = new TreeSet<>();
        negocios.forEach(n -> ids.add(n.getId()));
        return ids;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "tipoUsuario", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    @Mapping(target = "negocios", ignore = true)
    void updateEntity(@MappingTarget Usuario usuario, UsuarioRequestEditDTO dto);
}
