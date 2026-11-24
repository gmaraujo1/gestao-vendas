package br.com.gestao.gestaovendas.common.dto;

import br.com.gestao.gestaovendas.exception.RequisicaoInvalidaException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class Paginas {

    private Paginas() {
    }

    /**
     * Valida o ?sort= contra os campos permitidos (campo desconhecido = 400, e não 500)
     * e acrescenta "id" como último critério. Sem o desempate, nomes repetidos
     * (dois "João") podem aparecer em duas páginas ou sumir entre elas.
     */
    public static Pageable validar(Pageable pageable, Set<String> camposPermitidos) {
        for (Sort.Order ordem : pageable.getSort()) {
            if (!camposPermitidos.contains(ordem.getProperty())) {
                throw new RequisicaoInvalidaException("Campo de ordenação inválido: " + ordem.getProperty()
                        + ". Use um destes: " + String.join(", ", camposPermitidos));
            }
        }
        if (pageable.isUnpaged() || pageable.getSort().getOrderFor("id") != null) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().and(Sort.by("id")));
    }
}
