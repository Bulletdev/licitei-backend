package com.effecti.licitacoes.application.service;

import com.effecti.licitacoes.application.dto.LicitacaoDTO;
import com.effecti.licitacoes.application.dto.PageResponseDTO;
import com.effecti.licitacoes.domain.entity.Licitacao;
import com.effecti.licitacoes.domain.repository.LicitacaoRepository;
import com.effecti.licitacoes.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LicitacaoService {

    private final LicitacaoRepository repository;

    public LicitacaoService(LicitacaoRepository repository) {
        this.repository = repository;
    }

    public PageResponseDTO<LicitacaoDTO> findAll(Integer codigoUasg, String numeroPregao,
                                                 int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        Page<Licitacao> licitacoes = repository.findByFilters(codigoUasg, numeroPregao, pageable);
        Page<LicitacaoDTO> dtos = licitacoes.map(LicitacaoDTO::from);
        return PageResponseDTO.from(dtos);
    }

    public LicitacaoDTO findById(Long id) {
        Licitacao licitacao = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Licitação não encontrada"));
        return LicitacaoDTO.from(licitacao);
    }

    private Pageable createPageable(int page, int size, String sort) {
        Sort sorting = Sort.by(Sort.Direction.DESC, "criadoEm");
        if (sort != null && !sort.isBlank()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length == 2) {
                Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
                sorting = Sort.by(direction, sortParams[0]);
            }
        }
        return PageRequest.of(page, size, sorting);
    }
}