package br.com.gestao.gestaovendas.relatorio.repository;

import br.com.gestao.gestaovendas.common.dto.Periodo;
import br.com.gestao.gestaovendas.common.enums.UnidadeMedida;
import br.com.gestao.gestaovendas.estoque.domain.MotivoPerda;
import br.com.gestao.gestaovendas.relatorio.domain.CriterioRanking;
import br.com.gestao.gestaovendas.relatorio.dto.PerdaPorMotivoDTO;
import br.com.gestao.gestaovendas.relatorio.dto.PerdaPorProdutoDTO;
import br.com.gestao.gestaovendas.relatorio.dto.ProdutoVendidoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Consultas agregadas dos relatórios e do dashboard (só leitura, SQL nativo).
 * Toda consulta filtra por negocio_id.
 *
 * <p>Regras de valor (ver docs/ROADMAP.md, etapa 6):
 * <ul>
 *   <li>só vendas REGISTRADA entram; a cancelada sai do cálculo inteira;</li>
 *   <li>CMV = quantidade × custo_unitario guardado no item, arredondado por item;</li>
 *   <li>perdas = |quantidade| × custo_unitario das movimentações PERDA (avulsas e de trocas);</li>
 *   <li>despesas pela data da despesa.</li>
 * </ul>
 * O arredondamento é por linha para que a soma dos dias bata com o total do período.
 *
 * <p>Ainda não entram no resultado: o custo do produto entregue numa troca (TROCA_SAIDA) e as
 * diferenças de contagem (AJUSTE). Ver docs/ROADMAP.md.
 */
@Repository
@RequiredArgsConstructor
public class RelatorioRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public record VendasDoDia(LocalDate dia, long numeroVendas, BigDecimal vendas, BigDecimal cmv) {
    }

    public record ValorDoDia(LocalDate dia, BigDecimal valor) {
    }

    public List<VendasDoDia> vendasPorDia(Long negocioId, Periodo periodo) {
        return jdbc.query("""
                        select cast(v.data_hora as date) as dia,
                               count(distinct v.id) as numero_vendas,
                               sum(vi.subtotal) as vendas,
                               sum(round(vi.quantidade * vi.custo_unitario, 2)) as cmv
                        from venda v
                        join venda_item vi on vi.venda_id = v.id and vi.negocio_id = v.negocio_id
                        where v.negocio_id = :negocioId and v.status = 'REGISTRADA'
                          and v.data_hora >= :inicio and v.data_hora < :fim
                        group by cast(v.data_hora as date)
                        order by dia
                        """,
                parametros(negocioId, periodo),
                (rs, i) -> new VendasDoDia(rs.getObject("dia", LocalDate.class), rs.getLong("numero_vendas"),
                        rs.getBigDecimal("vendas"), rs.getBigDecimal("cmv")));
    }

    public List<ValorDoDia> despesasPorDia(Long negocioId, Periodo periodo) {
        return jdbc.query("""
                        select d.data as dia, sum(d.valor) as valor
                        from despesa d
                        where d.negocio_id = :negocioId and d.data >= :de and d.data <= :ate
                        group by d.data
                        order by d.data
                        """,
                parametros(negocioId, periodo),
                (rs, i) -> new ValorDoDia(rs.getObject("dia", LocalDate.class), rs.getBigDecimal("valor")));
    }

    public List<ValorDoDia> perdasPorDia(Long negocioId, Periodo periodo) {
        return jdbc.query("""
                        select cast(m.data_hora as date) as dia,
                               sum(round(-m.quantidade * m.custo_unitario, 2)) as valor
                        from movimentacao_estoque m
                        where m.negocio_id = :negocioId and m.tipo = 'PERDA'
                          and m.data_hora >= :inicio and m.data_hora < :fim
                        group by cast(m.data_hora as date)
                        order by dia
                        """,
                parametros(negocioId, periodo),
                (rs, i) -> new ValorDoDia(rs.getObject("dia", LocalDate.class), rs.getBigDecimal("valor")));
    }

    public List<PerdaPorMotivoDTO> perdasPorMotivo(Long negocioId, Periodo periodo) {
        return jdbc.query("""
                        select m.motivo_perda as motivo, count(*) as registros,
                               sum(round(-m.quantidade * m.custo_unitario, 2)) as custo
                        from movimentacao_estoque m
                        where m.negocio_id = :negocioId and m.tipo = 'PERDA'
                          and m.data_hora >= :inicio and m.data_hora < :fim
                        group by m.motivo_perda
                        order by custo desc, motivo
                        """,
                parametros(negocioId, periodo),
                (rs, i) -> new PerdaPorMotivoDTO(MotivoPerda.valueOf(rs.getString("motivo")),
                        rs.getLong("registros"), rs.getBigDecimal("custo")));
    }

    public List<PerdaPorProdutoDTO> perdasPorProduto(Long negocioId, Periodo periodo, int limite) {
        return jdbc.query("""
                        select p.id, p.codigo, p.descricao, p.unidade,
                               sum(-m.quantidade) as quantidade,
                               sum(round(-m.quantidade * m.custo_unitario, 2)) as custo
                        from movimentacao_estoque m
                        join produto p on p.id = m.produto_id and p.negocio_id = m.negocio_id
                        where m.negocio_id = :negocioId and m.tipo = 'PERDA'
                          and m.data_hora >= :inicio and m.data_hora < :fim
                        group by p.id, p.codigo, p.descricao, p.unidade
                        order by custo desc, p.id
                        limit :limite
                        """,
                parametros(negocioId, periodo).addValue("limite", limite),
                (rs, i) -> new PerdaPorProdutoDTO(rs.getLong("id"), rs.getInt("codigo"), rs.getString("descricao"),
                        UnidadeMedida.valueOf(rs.getString("unidade")), rs.getBigDecimal("quantidade"),
                        rs.getBigDecimal("custo")));
    }

    public List<ProdutoVendidoDTO> produtosMaisVendidos(Long negocioId, Periodo periodo, CriterioRanking criterio,
                                                        int limite) {
        // a ordem vem de um enum (texto fixo), nunca da requisição
        String ordem = switch (criterio) {
            case QUANTIDADE -> "sum(vi.quantidade) desc, sum(vi.subtotal) desc";
            case VENDAS -> "sum(vi.subtotal) desc";
            case LUCRO -> "(sum(vi.subtotal) - sum(round(vi.quantidade * vi.custo_unitario, 2))) desc";
        };
        return jdbc.query("""
                        select p.id, p.codigo, p.descricao, p.unidade,
                               sum(vi.quantidade) as quantidade,
                               sum(vi.subtotal) as vendas,
                               sum(round(vi.quantidade * vi.custo_unitario, 2)) as cmv
                        from venda v
                        join venda_item vi on vi.venda_id = v.id and vi.negocio_id = v.negocio_id
                        join produto p on p.id = vi.produto_id and p.negocio_id = v.negocio_id
                        where v.negocio_id = :negocioId and v.status = 'REGISTRADA'
                          and v.data_hora >= :inicio and v.data_hora < :fim
                        group by p.id, p.codigo, p.descricao, p.unidade
                        order by %s, p.id
                        limit :limite
                        """.formatted(ordem),
                parametros(negocioId, periodo).addValue("limite", limite),
                (rs, i) -> ProdutoVendidoDTO.de(rs.getLong("id"), rs.getInt("codigo"), rs.getString("descricao"),
                        UnidadeMedida.valueOf(rs.getString("unidade")), rs.getBigDecimal("quantidade"),
                        rs.getBigDecimal("vendas"), rs.getBigDecimal("cmv")));
    }

    private static MapSqlParameterSource parametros(Long negocioId, Periodo periodo) {
        return new MapSqlParameterSource()
                .addValue("negocioId", negocioId)
                .addValue("inicio", periodo.inicio())
                .addValue("fim", periodo.fim())
                .addValue("de", periodo.inicioData())
                .addValue("ate", periodo.fimData());
    }
}
