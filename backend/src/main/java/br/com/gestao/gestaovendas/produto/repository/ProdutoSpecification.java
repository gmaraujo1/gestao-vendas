package br.com.gestao.gestaovendas.produto.repository;

import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.dto.ProdutoFiltroDTO;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ProdutoSpecification {

    private ProdutoSpecification() {
    }

    public static Specification<Produto> comFiltros(ProdutoFiltroDTO filtro) {
        return Specification
                .where(descricaoContem(filtro.descricao()))
                .and(precoEntre(filtro.precoMinimo(), filtro.precoMaximo()))
                .and(atualizadoEntre(filtro.atualizadoDe(), filtro.atualizadoAte()))
                .and(criadoEntre(filtro.criadoDe(), filtro.criadoAte()));
    }

    private static Specification<Produto> descricaoContem(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }
        String termo = "%" + descricao.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("descricao")), termo);
    }

    private static Specification<Produto> precoEntre(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) {
            return null;
        }
        if (min != null && max != null) {
            return (root, query, cb) -> cb.between(root.get("precoMedio"), min, max);
        }
        if (min != null) {
            return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("precoMedio"), min);
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("precoMedio"), max);
    }

    private static Specification<Produto> atualizadoEntre(java.time.LocalDate de, java.time.LocalDate ate) {
        return periodoEntre("atualizadoEm", de, ate);
    }

    private static Specification<Produto> criadoEntre(java.time.LocalDate de, java.time.LocalDate ate) {
        return periodoEntre("criadoEm", de, ate);
    }

    private static Specification<Produto> periodoEntre(String campo, java.time.LocalDate de, java.time.LocalDate ate) {
        if (de == null && ate == null) {
            return null;
        }
        LocalDateTime inicio = de != null ? de.atStartOfDay() : null;
        LocalDateTime fim = ate != null ? LocalDateTime.of(ate, LocalTime.MAX) : null;

        if (inicio != null && fim != null) {
            return (root, query, cb) -> cb.between(root.get(campo), inicio, fim);
        }
        if (inicio != null) {
            return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(campo), inicio);
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(campo), fim);
    }
}
