package br.com.gestao.gestaovendas.produto.repository;

import br.com.gestao.gestaovendas.common.enums.Categoria;
import br.com.gestao.gestaovendas.common.enums.UnidadeMedida;
import br.com.gestao.gestaovendas.negocio.domain.Negocio;
import br.com.gestao.gestaovendas.negocio.repository.NegocioRepository;
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

    @Autowired
    private NegocioRepository negocioRepository;

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

        var filtro = new ProdutoFiltroDTO(null, new BigDecimal("10"), new BigDecimal("50"),
                null, null, null, null);
        List<Produto> resultado = produtoRepository.findAll(ProdutoSpecification.comFiltros(laticinio, filtro));

        assertThat(resultado).extracting(Produto::getDescricao).containsExactly("Queijo minas");
    }

    @Test
    void listagemNaoMisturaNegociosEMesmoCodigoPodeRepetir() {
        produtoRepository.save(produto(laticinio, 1, "Leite integral", "4.50"));
        produtoRepository.save(produto(paoNino, 1, "Pão de queijo", "1.20"));

        var semFiltro = new ProdutoFiltroDTO(null, null, null, null, null, null, null);

        assertThat(produtoRepository.findAll(ProdutoSpecification.comFiltros(paoNino, semFiltro)))
                .extracting(Produto::getDescricao).containsExactly("Pão de queijo");
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
        p.setCategoria(Categoria.ALIMENTOS);
        p.setUnidadeMedida(UnidadeMedida.UN);
        p.setPrecoMedio(new BigDecimal(preco));
        p.setCriadoEm(LocalDateTime.now());
        p.setAtualizadoEm(LocalDateTime.now());
        return p;
    }
}
