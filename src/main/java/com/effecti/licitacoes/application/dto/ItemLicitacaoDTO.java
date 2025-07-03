package com.effecti.licitacoes.application.dto;

import com.effecti.licitacoes.domain.entity.ItemLicitacao;

public record ItemLicitacaoDTO(
        Long id,
        Integer numeroItem,
        String descricao,
        Integer quantidade,
        String unidadeFornecimento
) {
    public static ItemLicitacaoDTO from(ItemLicitacao itemLicitacao) {
        return new ItemLicitacaoDTO(
                itemLicitacao.getId(),
                itemLicitacao.getNumeroItem(),
                itemLicitacao.getDescricao(),
                itemLicitacao.getQuantidade(),
                itemLicitacao.getUnidadeFornecimento()
        );
    }
} 