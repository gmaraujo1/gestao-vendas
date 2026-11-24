package br.com.gestao.gestaovendas.produto.repository;

import br.com.gestao.gestaovendas.common.enums.Categoria;
import br.com.gestao.gestaovendas.common.enums.UnidadeMedida;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.dto.ProdutoFiltroDTO;
import br.com.gestao.gestaovendas.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ProdutoRepositoryIntegrationTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    @BeforeEach
    void limpar() {
        produtoRepository.deleteAll();
    }

    @Test
    void persistePrecoMedioSemPerderCasasDecimais() {
        Produto salvo = produtoRepository.save(produto(1, "Leite integral", "4.3333"));

        Produto lido = produtoRepository.findById(salvo.getId()).orElseThrow();

        assertThat(lido.getPrecoMedio()).isEqualByComparingTo("4.3333");
    }

    @Test
    void filtraPorFaixaDePreco() {
        produtoRepository.save(produto(1, "Leite integral", "4.50"));
        produtoRepository.save(produto(2, "Queijo minas", "32.90"));

        var filtro = new ProdutoFiltroDTO(null, new BigDecimal("10"), new BigDecimal("50"),
                null, null, null, null);
        List<Produto> resultado = produtoRepository.findAll(ProdutoSpecification.comFiltros(filtro));

        assertThat(resultado).extracting(Produto::getDescricao).containsExactly("Queijo minas");
    }

    private Produto produto(int codigo, String descricao, String preco) {
        Produto p = new Produto();
        p.setCodigo(codigo);
        p.setDescricao(descricao);
        p.setCategoria(Categoria.ALIMENTOS);
        p.setUnidadeMedida(UnidadeMedida.UN);
        p.setPrecoMedio(new BigDecimal(preco));
        p.setCriadoEm(LocalDateTime.now());
        p.setAtualizadoEm(LocalDateTime.now());
        return p;
    }
}
