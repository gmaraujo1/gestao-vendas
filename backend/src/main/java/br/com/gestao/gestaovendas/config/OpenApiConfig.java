package br.com.gestao.gestaovendas.config;

import br.com.gestao.gestaovendas.negocio.context.NegocioAtual;
import br.com.gestao.gestaovendas.negocio.context.NegocioAtualArgumentResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Documentação OpenAPI. O front gera os tipos TypeScript a partir de /v3/api-docs
 * (npm run gen:api). Endpoints com {@link NegocioAtual} ganham o header X-Negocio-Id.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    static {
        // NegocioAtual é resolvido do header, não é parâmetro de query
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(NegocioAtual.class);
    }

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("Gestão Vendas API").version("v1")
                        .description("Laticínio e Pão Nino. Login em POST /api/auth/login; "
                                + "envie o token como Bearer e o negócio no header X-Negocio-Id."))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }

    @Bean
    OperationCustomizer headerNegocio() {
        return (operation, handlerMethod) -> {
            boolean usaNegocio = Arrays.stream(handlerMethod.getMethodParameters())
                    .anyMatch(p -> NegocioAtual.class.equals(p.getParameterType()));
            if (usaNegocio) {
                operation.addParametersItem(new HeaderParameter()
                        .name(NegocioAtualArgumentResolver.HEADER)
                        .required(true)
                        .description("Id do negócio ativo (GET /api/negocio/meus)")
                        .schema(new IntegerSchema().format("int64")));
            }
            return operation;
        };
    }
}
