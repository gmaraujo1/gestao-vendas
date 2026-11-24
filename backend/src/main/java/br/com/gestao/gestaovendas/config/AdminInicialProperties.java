package br.com.gestao.gestaovendas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin-inicial")
public record AdminInicialProperties(String nome, String email, String senha) {

    public boolean configurado() {
        return email != null && !email.isBlank() && senha != null && !senha.isBlank();
    }
}
