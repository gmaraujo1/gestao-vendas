package br.com.gestao.gestaovendas.negocio.context;

import br.com.gestao.gestaovendas.common.security.UsuarioLogado;
import br.com.gestao.gestaovendas.exception.AccessRecusedException;
import br.com.gestao.gestaovendas.exception.RequisicaoInvalidaException;
import br.com.gestao.gestaovendas.negocio.repository.NegocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class NegocioAtualArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String HEADER = "X-Negocio-Id";

    private final NegocioRepository negocioRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return NegocioAtual.class.equals(parameter.getParameterType());
    }

    @Override
    public NegocioAtual resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Long negocioId = lerHeader(webRequest.getHeader(HEADER));
        Long usuarioId = UsuarioLogado.id();
        if (!negocioRepository.usuarioTemAcesso(usuarioId, negocioId)) {
            throw new AccessRecusedException("Você não tem acesso a este negócio");
        }
        return new NegocioAtual(negocioId);
    }

    private static Long lerHeader(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RequisicaoInvalidaException("Informe o negócio no header " + HEADER);
        }
        try {
            return Long.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            throw new RequisicaoInvalidaException("Header " + HEADER + " inválido");
        }
    }
}
