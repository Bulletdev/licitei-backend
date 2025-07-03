package com.effecti.licitacoes.application.dto;

import com.effecti.licitacoes.domain.entity.Licitacao;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record LicitacaoDTO(
        Long id,
        Integer codigoUasg,
        String numeroPregao,
        String objeto,
        String dataAbertura,
        String modalidade,
        String endereco,
        List<ItemLicitacaoDTO> itens,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime criadoEm,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime atualizadoEm
) {
    public static LicitacaoDTO from(Licitacao licitacao) {
        return new LicitacaoDTO(
                licitacao.getId(),
                licitacao.getCodigoUasg(),
                licitacao.getNumeroPregao(),
                licitacao.getObjeto(),
                licitacao.getDataAbertura(),
                licitacao.getModalidade(),
                licitacao.getEndereco(),
                licitacao.getItens().stream()
                        .map(ItemLicitacaoDTO::from)
                        .collect(Collectors.toList()),
                licitacao.getCriadoEm(),
                licitacao.getAtualizadoEm()
        );
    }
}