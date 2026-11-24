package br.com.gestao.gestaovendas.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Os testes de integração compartilham o mesmo banco (contexto em cache) e rodam em ordem
 * que muda de máquina para máquina. Testes que precisam de "nenhum produto" usam isto em vez
 * de produtoRepository.deleteAll(), que falha quando outra classe já gravou movimentações
 * (a FK impede apagar produto com histórico, de propósito).
 */
@Component
public class LimpezaBanco {

    private final JdbcTemplate jdbc;

    public LimpezaBanco(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Apaga produtos e tudo que depende deles (movimentações, vendas, trocas). */
    public void apagarProdutosEHistorico() {
        jdbc.execute("TRUNCATE movimentacao_estoque, venda_item, venda, troca, produto");
    }
}
