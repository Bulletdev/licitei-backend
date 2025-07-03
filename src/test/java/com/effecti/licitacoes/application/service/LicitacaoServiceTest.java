package com.effecti.licitacoes.application.service;

import com.effecti.licitacoes.application.dto.LicitacaoDTO;
import com.effecti.licitacoes.application.dto.PageResponseDTO;
import com.effecti.licitacoes.domain.entity.Licitacao;
import com.effecti.licitacoes.domain.repository.LicitacaoRepository;
import com.effecti.licitacoes.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.function.Executable;

@ExtendWith(MockitoExtension.class)
class LicitacaoServiceTest {

    @Mock
    private LicitacaoRepository repository;

    @InjectMocks
    private LicitacaoService service;

    @Test
    void findAll_DeveRetornarPageResponseDTO() {
        Licitacao licitacao = new Licitacao(123456, "001/2024", "Objeto teste", "2024-01-01 10:00:00", "PREGAO", "Endereço teste");
        licitacao.setId(1L);

        Page<Licitacao> page = new PageImpl<>(List.of(licitacao), PageRequest.of(0, 20), 1);

        when(repository.findByFilters(eq(123456), eq("001/2024"), any(Pageable.class)))
                .thenReturn(page);

        PageResponseDTO<LicitacaoDTO> result = service.findAll(123456, "001/2024", 0, 20, null);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).codigoUasg()).isEqualTo(123456);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void findById_LicitacaoExiste_DeveRetornarDTO() {
        Licitacao licitacao = new Licitacao(123456, "001/2024", "Objeto teste", "2024-01-01 10:00:00", "PREGAO", "Endereço teste");
        licitacao.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(licitacao));

        LicitacaoDTO result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.codigoUasg()).isEqualTo(123456);
    }

    @Test
    void findById_LicitacaoNaoExiste_DeveLancarExcecao() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, (Executable) () -> service.findById(999L));
    }
}