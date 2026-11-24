package br.com.gestao.gestaovendas.produto.repository;

import br.com.gestao.gestaovendas.categoria.domain.Categoria;
import br.com.gestao.gestaovendas.categoria.repository.CategoriaRepository;
import br.com.gestao.gestaovendas.common.enums.UnidadeMedida;
import br.com.gestao.gestaovendas.negocio.domain.Negocio;
import br.com.gestao.gestaovendas.negocio.repository.NegocioRepository;
import br.com.gestao.gestaovendas.produto.domain.Produto;
import br.com.gestao.gestaovendas.produto.dto.ProdutoFiltroDTO;
import br.com.gestao.gestaovendas.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ProdutoRepositoryIntegrationTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private NegocioRepository negocioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Long laticinio;
    private Long paoNino;

    @BeforeEach
    void limpar() {
        produtoRepository.deleteAll();
        laticinio = idDo("LATICINIO");
        paoNino = idDo("PAO_NINO");
    }

    @Test
    void persistePrecoMedioSemPerderCasasDecimais() {
        Produto salvo = produtoRepository.save(produto(laticinio, 1, "Leite integral", "4.3333"));

        Produto lido = produtoRepository.findById(salvo.getId()).orElseThrow();

        assertThat(lido.getPrecoMedio()).isEqualByComparingTo("4.3333");
    }

    @Test
    void filtraPorFaixaDePreco() {
        produtoRepository.save(produto(laticinio, 1, "Leite integral", "4.50"));
        produtoRepository.save(produto(laticinio, 2, "Queijo minas", "32.90"));

        var filtro = new ProdutoFiltroDTO(null, null, null, new BigDecimal("10"), new BigDecimal("50"),
                null, null, null, null);
        List<Produto> resultado = produtoRepository.findAll(ProdutoSpecification.comFiltros(laticinio, filtro), Pageable.unpaged()).getContent();

        assertThat(resultado).extracting(Produto::getDescricao).containsExactly("Queijo minas");
    }

    @Test
    void listagemNaoMisturaNegociosEMesmoCodigoPodeRepetir() {
        produtoRepository.save(produto(laticinio, 1, "Leite integral", "4.50"));
        produtoRepository.save(produto(paoNino, 1, "Pão de queijo", "1.20"));

        var semFiltro = ProdutoFiltroDTO.vazio();

        assertThat(produtoRepository.findAll(ProdutoSpecification.comFiltros(paoNino, semFiltro), Pageable.unpaged()).getContent())
                .extracting(Produto::getDescricao).containsExactly("Pão de queijo");
    }

    @Test
    void buscaPorDescricaoTrataPorcentagemEUnderscoreComoTexto() {
        produtoRepository.save(produto(laticinio, 1, "Leite 100% integral", "4.50"));
        produtoRepository.save(produto(laticinio, 2, "Leite 1000 ml", "4.50"));

        var filtro = new ProdutoFiltroDTO("100%", null, null, null, null, null, null, null, null);

        assertThat(produtoRepository.findAll(ProdutoSpecification.comFiltros(laticinio, filtro), Pageable.unpaged()).getContent())
                .extracting(Produto::getDescricao).containsExactly("Leite 100% integral");
    }

    private Categoria categoriaDo(Long negocioId) {
        return categoriaRepository.findAllByNegocioIdOrderByNomeAsc(negocioId).stream().findFirst()
                .orElseGet(() -> {
                    Categoria c = new Categoria();
                    c.setNegocioId(negocioId);
                    c.setNome("Geral");
                    return categoriaRepository.save(c);
                });
    }

    private Long idDo(String codigo) {
        return negocioRepository.findAll().stream()
                .filter(n -> n.getCodigo().equals(codigo))
                .map(Negocio::getId)
                .findFirst().orElseThrow();
    }

    private Produto produto(Long negocioId, int codigo, String descricao, String preco) {
        Produto p = new Produto();
        p.setNegocioId(negocioId);
        p.setCodigo(codigo);
        p.setDescricao(descricao);
        p.setCategoria(categoriaDo(negocioId));
        p.setUnidadeMedida(UnidadeMedida.UN);
        p.setPrecoMedio(new BigDecimal(preco));
        return p;
    }
}
