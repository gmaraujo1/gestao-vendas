package br.com.gestao.gestaovendas.relatorio.pdf;

import br.com.gestao.gestaovendas.despesa.domain.TipoDespesa;
import br.com.gestao.gestaovendas.despesa.dto.ResumoDespesasDTO;
import br.com.gestao.gestaovendas.estoque.domain.MotivoPerda;
import br.com.gestao.gestaovendas.relatorio.domain.TipoPeriodo;
import br.com.gestao.gestaovendas.relatorio.dto.PerdaPorMotivoDTO;
import br.com.gestao.gestaovendas.relatorio.dto.PontoSerieDTO;
import br.com.gestao.gestaovendas.relatorio.dto.ProdutoVendidoDTO;
import br.com.gestao.gestaovendas.relatorio.dto.RelatorioLucroDTO;
import br.com.gestao.gestaovendas.relatorio.dto.ResultadoDTO;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.HeaderFooter;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Desenha o relatório de lucro e prejuízo em PDF (A4). Recebe os números prontos
 * ({@link RelatorioLucroDTO}); aqui só tem formatação.
 */
@Component
public class RelatorioLucroPdf {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_CURTA = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private static final Color VERDE = new Color(0x1B, 0x7F, 0x3B);
    private static final Color VERMELHO = new Color(0xB4, 0x23, 0x18);
    private static final Color CINZA = new Color(0x6B, 0x72, 0x80);
    private static final Color FUNDO_CABECALHO = new Color(0xF1, 0xF3, 0xF5);
    private static final Color BORDA = new Color(0xD8, 0xDC, 0xE0);

    private static final Font TITULO = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 11, Font.NORMAL);
    private static final Font SECAO = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font TEXTO = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font NEGRITO = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font NOTA = new Font(Font.HELVETICA, 8, Font.ITALIC, CINZA);

    private static final Map<TipoDespesa, String> TIPOS_DESPESA = Map.of(
            TipoDespesa.GASOLINA, "Gasolina",
            TipoDespesa.PEDAGIO, "Pedágio",
            TipoDespesa.PNEU, "Pneu",
            TipoDespesa.MECANICA, "Mecânica",
            TipoDespesa.OUTROS, "Outros");

    private static final Map<MotivoPerda, String> MOTIVOS_PERDA = Map.of(
            MotivoPerda.FURADO, "Furado",
            MotivoPerda.VENCIDO, "Vencido",
            MotivoPerda.AVARIADO, "Avariado",
            MotivoPerda.OUTRO, "Outro");

    private static final Map<TipoPeriodo, String> DETALHE = Map.of(
            TipoPeriodo.DIA, "por dia",
            TipoPeriodo.SEMANA, "por semana",
            TipoPeriodo.QUINZENA, "por quinzena",
            TipoPeriodo.MES, "por mês",
            TipoPeriodo.TRIMESTRE, "por trimestre",
            TipoPeriodo.SEMESTRE, "por semestre",
            TipoPeriodo.ANO, "por ano");

    public byte[] gerar(String negocio, RelatorioLucroDTO r, LocalDateTime geradoEm) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 50);
        PdfWriter.getInstance(doc, saida);
        doc.addTitle("Relatório de lucro e prejuízo - " + negocio);
        HeaderFooter rodape = new HeaderFooter(new Phrase(negocio + " · " + r.descricaoPeriodo() + " · página ", NOTA), true);
        rodape.setAlignment(Element.ALIGN_CENTER);
        rodape.setBorder(Rectangle.NO_BORDER);
        doc.setFooter(rodape);
        doc.open();

        doc.add(new Paragraph("Relatório de lucro e prejuízo", TITULO));
        doc.add(new Paragraph(negocio + " · " + r.descricaoPeriodo()
                + " (" + DATA.format(r.de()) + " a " + DATA.format(r.ate()) + ")", SUBTITULO));
        doc.add(new Paragraph("Gerado em " + DATA_HORA.format(geradoEm), NOTA));

        resumo(doc, r.resultado());
        detalhamento(doc, r);
        despesas(doc, r.despesasPorTipo());
        perdas(doc, r.perdasPorMotivo());
        produtos(doc, r.produtosMaisVendidos());

        doc.close();
        return saida.toByteArray();
    }

    private void resumo(Document doc, ResultadoDTO r) {
        secao(doc, "Resumo");
        PdfPTable t = tabela(new float[]{3, 1.3f});
        linhaResumo(t, "Vendas (" + r.numeroVendas() + (r.numeroVendas() == 1 ? " venda)" : " vendas)"),
                r.vendas(), TEXTO, null);
        linhaResumo(t, "(-) Custo das mercadorias vendidas", r.cmv(), TEXTO, null);
        linhaResumo(t, "(=) Lucro bruto", r.lucroBruto(), NEGRITO, null);
        linhaResumo(t, "(-) Despesas", r.despesas(), TEXTO, null);
        boolean lucro = r.resultado().signum() >= 0;
        linhaResumo(t, lucro ? "(=) Lucro do período" : "(=) Prejuízo do período", r.resultado(),
                new Font(Font.HELVETICA, 11, Font.BOLD, lucro ? VERDE : VERMELHO), FUNDO_CABECALHO);
        linhaResumo(t, "(-) Perdas (custo dos produtos perdidos)", r.perdas(), TEXTO, null);
        boolean lucroComPerdas = r.resultadoComPerdas().signum() >= 0;
        linhaResumo(t, "(=) Resultado considerando as perdas", r.resultadoComPerdas(),
                new Font(Font.HELVETICA, 9, Font.BOLD, lucroComPerdas ? VERDE : VERMELHO), null);
        doc.add(t);
        doc.add(new Paragraph("Vendas canceladas não entram. O custo de cada venda é o custo médio do produto "
                + "no momento da venda. Perdas incluem os produtos devolvidos em trocas que viraram perda.", NOTA));
    }

    private void detalhamento(Document doc, RelatorioLucroDTO r) {
        if (r.detalhamento().size() <= 1) {
            return;
        }
        // no papel, faixas sem nenhum lançamento só ocupam espaço (o gráfico do dashboard mantém todas)
        List<PontoSerieDTO> comMovimento = r.detalhamento().stream().filter(RelatorioLucroPdf::teveMovimento).toList();
        secao(doc, "Detalhamento " + DETALHE.get(r.agrupamento()));
        if (comMovimento.isEmpty()) {
            doc.add(new Paragraph("Nenhum lançamento no período.", TEXTO));
            return;
        }
        PdfPTable t = tabela(new float[]{1.6f, 1, 1, 1, 1, 1.1f});
        cabecalho(t, "Período", "Vendas", "Custo", "Despesas", "Perdas", "Resultado");
        for (PontoSerieDTO p : comMovimento) {
            celula(t, faixa(p, r.agrupamento()), TEXTO, Element.ALIGN_LEFT);
            celula(t, dinheiro(p.vendas()), TEXTO, Element.ALIGN_RIGHT);
            celula(t, dinheiro(p.cmv()), TEXTO, Element.ALIGN_RIGHT);
            celula(t, dinheiro(p.despesas()), TEXTO, Element.ALIGN_RIGHT);
            celula(t, dinheiro(p.perdas()), TEXTO, Element.ALIGN_RIGHT);
            celula(t, dinheiro(p.resultado()), corDoValor(p.resultado()), Element.ALIGN_RIGHT);
        }
        doc.add(t);
        if (comMovimento.size() < r.detalhamento().size()) {
            doc.add(new Paragraph("Períodos sem nenhum lançamento foram omitidos.", NOTA));
        }
    }

    private static boolean teveMovimento(PontoSerieDTO p) {
        return p.numeroVendas() > 0 || p.despesas().signum() != 0 || p.perdas().signum() != 0;
    }

    private void despesas(Document doc, List<ResumoDespesasDTO.PorTipo> porTipo) {
        secao(doc, "Despesas por tipo");
        if (porTipo.isEmpty()) {
            doc.add(new Paragraph("Nenhuma despesa no período.", TEXTO));
            return;
        }
        PdfPTable t = tabela(new float[]{3, 1.3f});
        cabecalho(t, "Tipo", "Total");
        for (ResumoDespesasDTO.PorTipo d : porTipo) {
            celula(t, TIPOS_DESPESA.get(d.tipo()), TEXTO, Element.ALIGN_LEFT);
            celula(t, dinheiro(d.total()), TEXTO, Element.ALIGN_RIGHT);
        }
        doc.add(t);
    }

    private void perdas(Document doc, List<PerdaPorMotivoDTO> porMotivo) {
        secao(doc, "Perdas por motivo");
        if (porMotivo.isEmpty()) {
            doc.add(new Paragraph("Nenhuma perda no período.", TEXTO));
            return;
        }
        PdfPTable t = tabela(new float[]{2, 1, 1.3f});
        cabecalho(t, "Motivo", "Lançamentos", "Custo");
        for (PerdaPorMotivoDTO p : porMotivo) {
            celula(t, MOTIVOS_PERDA.get(p.motivo()), TEXTO, Element.ALIGN_LEFT);
            celula(t, String.valueOf(p.registros()), TEXTO, Element.ALIGN_RIGHT);
            celula(t, dinheiro(p.custo()), TEXTO, Element.ALIGN_RIGHT);
        }
        doc.add(t);
    }

    private void produtos(Document doc, List<ProdutoVendidoDTO> produtos) {
        secao(doc, "Produtos que mais venderam");
        if (produtos.isEmpty()) {
            doc.add(new Paragraph("Nenhuma venda no período.", TEXTO));
            return;
        }
        PdfPTable t = tabela(new float[]{2.6f, 1, 1.1f, 1.1f, 1.1f});
        cabecalho(t, "Produto", "Quantidade", "Vendas", "Custo", "Lucro bruto");
        for (ProdutoVendidoDTO p : produtos) {
            celula(t, p.descricao(), TEXTO, Element.ALIGN_LEFT);
            celula(t, quantidade(p.quantidade()) + " " + p.unidadeMedida(), TEXTO, Element.ALIGN_RIGHT);
            celula(t, dinheiro(p.vendas()), TEXTO, Element.ALIGN_RIGHT);
            celula(t, dinheiro(p.cmv()), TEXTO, Element.ALIGN_RIGHT);
            celula(t, dinheiro(p.lucroBruto()), corDoValor(p.lucroBruto()), Element.ALIGN_RIGHT);
        }
        doc.add(t);
    }

    private static void secao(Document doc, String titulo) {
        Paragraph p = new Paragraph(titulo, SECAO);
        p.setSpacingBefore(14);
        p.setSpacingAfter(6);
        doc.add(p);
    }

    private static PdfPTable tabela(float[] larguras) {
        PdfPTable t = new PdfPTable(larguras);
        t.setWidthPercentage(100);
        return t;
    }

    private static void cabecalho(PdfPTable t, String... titulos) {
        for (int i = 0; i < titulos.length; i++) {
            PdfPCell c = celulaBase(titulos[i], NEGRITO, i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            c.setBackgroundColor(FUNDO_CABECALHO);
            t.addCell(c);
        }
        t.setHeaderRows(1);
    }

    private static void linhaResumo(PdfPTable t, String rotulo, BigDecimal valor, Font fonte, Color fundo) {
        PdfPCell r = celulaBase(rotulo, fonte, Element.ALIGN_LEFT);
        PdfPCell v = celulaBase(dinheiro(valor), fonte, Element.ALIGN_RIGHT);
        if (fundo != null) {
            r.setBackgroundColor(fundo);
            v.setBackgroundColor(fundo);
        }
        t.addCell(r);
        t.addCell(v);
    }

    private static void celula(PdfPTable t, String texto, Font fonte, int alinhamento) {
        t.addCell(celulaBase(texto, fonte, alinhamento));
    }

    private static PdfPCell celulaBase(String texto, Font fonte, int alinhamento) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fonte));
        c.setHorizontalAlignment(alinhamento);
        c.setPaddingTop(4);
        c.setPaddingBottom(5);
        c.setPaddingLeft(6);
        c.setPaddingRight(6);
        c.setBorderColor(BORDA);
        return c;
    }

    private static Font corDoValor(BigDecimal valor) {
        return valor.signum() < 0 ? new Font(Font.HELVETICA, 9, Font.NORMAL, VERMELHO) : TEXTO;
    }

    private static String faixa(PontoSerieDTO p, TipoPeriodo agrupamento) {
        if (p.inicio().equals(p.fim())) {
            return DATA.format(p.inicio());
        }
        if (agrupamento == TipoPeriodo.MES && p.inicio().getDayOfMonth() == 1
                && p.fim().equals(p.inicio().withDayOfMonth(p.inicio().lengthOfMonth()))) {
            return DateTimeFormatter.ofPattern("MM/yyyy").format(p.inicio());
        }
        return DATA_CURTA.format(p.inicio()) + " a " + DATA.format(p.fim());
    }

    /** NumberFormat não é thread-safe: um por chamada. */
    private static String dinheiro(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    private static String quantidade(BigDecimal valor) {
        return new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(PT_BR)).format(valor);
    }
}
