package br.com.gestao.gestaovendas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiracao) {

    private static final int TAMANHO_MINIMO_SECRET = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SECRET) {
            throw new IllegalStateException(
                    "app.jwt.secret (JWT_SECRET) é obrigatório e precisa ter pelo menos "
                            + TAMANHO_MINIMO_SECRET + " bytes");
        }
        if (expiracao == null) {
            expiracao = Duration.ofHours(12);
        }
    }
}
