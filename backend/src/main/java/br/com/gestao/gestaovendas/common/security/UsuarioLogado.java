package br.com.gestao.gestaovendas.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/** Lê o usuário autenticado do JWT (subject = id do usuário). */
public final class UsuarioLogado {

    private UsuarioLogado() {
    }

    public static Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Requisição sem usuário autenticado");
        }
        return Long.valueOf(jwt.getSubject());
    }
}
