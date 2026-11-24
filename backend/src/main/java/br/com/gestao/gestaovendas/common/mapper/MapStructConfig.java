package br.com.gestao.gestaovendas.common.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Configuração compartilhada por todos os mappers do sistema.
 * Novos mappers devem usar {@code @Mapper(config = MapStructConfig.class)}.
 */
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface MapStructConfig {
}
