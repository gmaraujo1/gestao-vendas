package br.com.gestao.gestaovendas.negocio.context;

/**
 * Negócio em que a requisição opera, vindo do header {@value NegocioAtualArgumentResolver#HEADER}
 * e já validado contra os vínculos do usuário logado. Declare-o como parâmetro do endpoint.
 */
public record NegocioAtual(Long id) {
}
