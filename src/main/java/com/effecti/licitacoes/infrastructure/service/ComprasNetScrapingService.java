package com.effecti.licitacoes.infrastructure.service;

import com.effecti.licitacoes.domain.entity.Licitacao;
import com.effecti.licitacoes.domain.entity.ItemLicitacao;
import com.effecti.licitacoes.domain.repository.LicitacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Connection;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ComprasNetScrapingService {

    private static final Logger logger = LoggerFactory.getLogger(ComprasNetScrapingService.class);
    private static final String COMPRASNET_BASE_URL = "http://comprasnet.gov.br";
    private static final String COMPRASNET_LICITACOES_URL = COMPRASNET_BASE_URL + "/ConsultaLicitacoes/ConsLicitacaoDia.asp";
    private static final String COMPRASNET_DETALHES_URL_FORMAT = COMPRASNET_BASE_URL + "/ConsultaLicitacoes/download/download_editais_detalhe.asp?coduasg=%s&modprp=%s&numprp=%s";

    private final LicitacaoRepository repository;

    public ComprasNetScrapingService(LicitacaoRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void capturarLicitacoes() {
        try {
            Connection.Response response = Jsoup.connect(COMPRASNET_LICITACOES_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                    .timeout(60 * 1000)
                    .execute();

            Document doc = Jsoup.parse(new ByteArrayInputStream(response.bodyAsBytes()), "iso-8859-1", COMPRASNET_LICITACOES_URL);

            List<Licitacao> novasLicitacoes = new ArrayList<>();

            Elements licitacaoForms = doc.select("form[name^=Form]");

            if (licitacaoForms.isEmpty()) {
                logger.warn("Nenhum formulário de licitação encontrado. Verifique o HTML da página ou o seletor.");
            }

            for (Element form : licitacaoForms) {
                try {
                    Element tdTex3 = form.selectFirst("table.td tr.tex3 td");

                    if (tdTex3 == null) {
                        logger.warn("TD com o objeto da licitação não encontrado no formulário: {}", form.html());
                        continue;
                    }

                    String htmlContent = tdTex3.html();
                    logger.debug("Conteúdo do htmlContent para licitação: {}", htmlContent);

                    String codigoUasgStr = extractTextByRegex(htmlContent, "C\\u00F3digo da UASG: (\\d+)");
                    Integer codigoUasg = codigoUasgStr != null ? Integer.parseInt(codigoUasgStr) : null;

                    String numeroPregao = extractTextByRegex(htmlContent, "Preg\\u00E3o Eletr\\u00F4nico N\\u00BA\\s*(\\d+\\/\\d{4})");

                    String objeto = extractTextByRegex(htmlContent, "<b>Objeto:</b>\\s*.*?\\s*([\\s\\S]*?)(?=<br><b>Edital a partir de:</b>)");
                    if (objeto != null) { objeto = objeto.trim(); }

                    String dataAbertura = extractTextByRegex(htmlContent, "Edital a partir de:</b>&nbsp;(\\d{2}\\/\\d{2}\\/\\d{4})");
                    String modalidade = "Pregão Eletrônico";
                    String endereco = extractTextByRegex(htmlContent, "Endere\\u00E7o:</b>\\s*([\\s\\S]*?)(?=<br><b>Telefone:</b>)");
                    if (endereco != null) { endereco = endereco.trim().replaceAll("\\s{2,}", " ").replace("-", "").trim(); }

                    Element itensButton = form.selectFirst("input[name=itens]");
                    String codUasgItem = null;
                    String modPrpItem = null;
                    String numPrpItem = null;

                    if (itensButton != null) {
                        String onClick = itensButton.attr("onClick");
                        Pattern pattern = Pattern.compile("coduasg=(\\d+)&modprp=(\\d+)&numprp=(\\d+)");
                        Matcher matcher = pattern.matcher(onClick);
                        if (matcher.find()) {
                            codUasgItem = matcher.group(1);
                            modPrpItem = matcher.group(2);
                            numPrpItem = matcher.group(3);
                        }
                    }

                    if (codigoUasg != null && numeroPregao != null && objeto != null && dataAbertura != null && endereco != null) {
                        if (!repository.existsByCodigoUasgAndNumeroPregao(codigoUasg, numeroPregao)) {
                            Licitacao licitacao = new Licitacao(
                                    codigoUasg, numeroPregao, objeto, dataAbertura, modalidade, endereco);

                            if (codUasgItem != null && modPrpItem != null && numPrpItem != null) {
                                List<ItemLicitacao> itens = extrairItensLicitacao(codUasgItem, modPrpItem, numPrpItem);
                                for (ItemLicitacao item : itens) {
                                    licitacao.addItem(item);
                                }
                            }
                            novasLicitacoes.add(licitacao);
                        }
                    } else {
                        logger.warn("Dados incompletos para uma licitação (UASG, Pregão, Objeto, Data Abertura ou Endereço ausente).");
                    }

                } catch (Exception e) {
                    logger.error("Erro ao parsear formulário de licitação: {}", form.html(), e);
                }
            }

            logger.info("Total de novas licitações para salvar: {}", novasLicitacoes.size());

            if (!novasLicitacoes.isEmpty()) {
                repository.saveAll(novasLicitacoes);
                logger.info("Capturadas {} novas licitações do ComprasNet", novasLicitacoes.size());
            } else {
                logger.info("Nenhuma nova licitação encontrada no ComprasNet");
            }

        } catch (IOException e) {
            logger.error("Erro ao conectar ou ler a página do ComprasNet", e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao capturar licitações via scraping", e);
        }
    }

    private String extractTextByRegex(String html, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private List<ItemLicitacao> extrairItensLicitacao(String codUasg, String modPrp, String numPrp) throws IOException {
        String itemDetailsUrl = String.format(COMPRASNET_DETALHES_URL_FORMAT, codUasg, modPrp, numPrp);

        Connection.Response itemResponse = Jsoup.connect(itemDetailsUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                .timeout(60 * 1000)
                .execute();

        Document itemDoc = Jsoup.parse(new ByteArrayInputStream(itemResponse.bodyAsBytes()), "iso-8859-1", itemDetailsUrl);

        List<ItemLicitacao> itens = new ArrayList<>();

        // Seleciona diretamente as TDs que contêm os detalhes dos itens.
        // Essas TDs estão dentro de uma tabela com width='100%' que por sua vez está dentro de uma TD com class='tex3'
        Elements itemDetailCells = itemDoc.select("td.tex3 table[width='100%'] td[width='650']");

        for (Element itemCell : itemDetailCells) {
            try {
                // Pula se for uma seção de "Grupos"
                if (itemCell.selectFirst("span.tex3b:contains(Grupos)") != null) {
                    logger.debug("Pulando seção 'Grupos'. HTML: {}", itemCell.html());
                    continue;
                }

                logger.debug("Processando itemCell: {}", itemCell.html());

                Integer numeroItem = null;
                String descricao = null;
                Integer quantidade = null;
                String unidadeFornecimento = null;

                // Pega o span.tex3b para número e descrição
                String numeroItemDescricao = itemCell.selectFirst("span.tex3b") != null ? itemCell.selectFirst("span.tex3b").text() : null;
                logger.debug("numeroItemDescricao bruto: {}", numeroItemDescricao);

                // Pega o span.tex3 para descrição detalhada, quantidade e unidade
                String descricaoDetalhadaCompleta = itemCell.selectFirst("span.tex3") != null ? itemCell.selectFirst("span.tex3").html() : null;
                logger.debug("descricaoDetalhadaCompleta bruta: {}", descricaoDetalhadaCompleta);

                if (numeroItemDescricao != null) {
                    Pattern numeroDescricaoPattern = Pattern.compile("(\\d+) - (.*)");
                    Matcher numeroDescricaoMatcher = numeroDescricaoPattern.matcher(numeroItemDescricao);
                    if (numeroDescricaoMatcher.find()) {
                        logger.debug("Numero/Descricao Matcher found. Group 1: {}, Group 2: {}", numeroDescricaoMatcher.group(1), numeroDescricaoMatcher.group(2));
                        numeroItem = Integer.parseInt(numeroDescricaoMatcher.group(1));
                        descricao = numeroDescricaoMatcher.group(2).trim();
                    } else {
                        logger.warn("Regex para Numero/Descricao não encontrou correspondência em: {}", numeroItemDescricao);
                    }
                }

                if (descricaoDetalhadaCompleta != null) {
                    Pattern quantidadePattern = Pattern.compile("Quantidade: (\\d+)");
                    Matcher quantidadeMatcher = quantidadePattern.matcher(descricaoDetalhadaCompleta);
                    if (quantidadeMatcher.find()) {
                        logger.debug("Quantidade Matcher found. Group 1: {}", quantidadeMatcher.group(1));
                        quantidade = Integer.parseInt(quantidadeMatcher.group(1));
                    } else {
                        logger.warn("Regex para Quantidade não encontrou correspondência em: {}", descricaoDetalhadaCompleta);
                    }

                    Pattern unidadeFornecimentoPattern = Pattern.compile("Unidade de fornecimento: ([^<]+)");
                    Matcher unidadeFornecimentoMatcher = unidadeFornecimentoPattern.matcher(descricaoDetalhadaCompleta);
                    if (unidadeFornecimentoMatcher.find()) {
                        logger.debug("Unidade de Fornecimento Matcher found. Group 1: {}", unidadeFornecimentoMatcher.group(1));
                        unidadeFornecimento = unidadeFornecimentoMatcher.group(1).trim();
                    } else {
                        logger.warn("Regex para Unidade de Fornecimento não encontrou correspondência em: {}", descricaoDetalhadaCompleta);
                    }
                }

                if (numeroItem != null && descricao != null && quantidade != null && unidadeFornecimento != null) {
                    ItemLicitacao itemLicitacao = new ItemLicitacao();
                    itemLicitacao.setNumeroItem(numeroItem);
                    itemLicitacao.setDescricao(descricao);
                    itemLicitacao.setQuantidade(quantidade);
                    itemLicitacao.setUnidadeFornecimento(unidadeFornecimento);
                    itens.add(itemLicitacao);
                } else {
                    logger.warn("Dados incompletos para um item da licitação. Numero: {}, Descricao: {}, Quantidade: {}, Unidade: {}",
                            numeroItem, descricao, quantidade, unidadeFornecimento);
                }
            } catch (Exception e) {
                logger.error("Erro ao processar item: {}", itemCell.html(), e);
            }
        }

        return itens;
    }
}