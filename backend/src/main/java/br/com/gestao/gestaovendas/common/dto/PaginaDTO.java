package br.com.gestao.gestaovendas.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Resposta paginada estável (o JSON do Page do Spring não é garantido entre versões). */
public record PaginaDTO<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas
) {

    public static <E, T> PaginaDTO<T> de(Page<E> page, Function<E, T> mapper) {
        return new PaginaDTO<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
