package br.com.gestao.gestaovendas.common.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Configuração compartilhada por todos os mappers do sistema.
 * Novos mappers devem usar {@code @Mapper(config = MapStructConfig.class)}.
 */
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        // PATCH: campo ausente (null) no DTO mantém o valor atual da entidade
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MapStructConfig {
}
