package com.effecti.licitacoes.presentation.controller;

import com.effecti.licitacoes.application.dto.LicitacaoDTO;
import com.effecti.licitacoes.application.dto.PageResponseDTO;
import com.effecti.licitacoes.application.service.LicitacaoService;
import com.effecti.licitacoes.infrastructure.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LicitacaoController.class)
class LicitacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LicitacaoService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listarLicitacoes_DeveRetornarListaPaginada() throws Exception {
        LicitacaoDTO dto = new LicitacaoDTO(
                1L, 123456, "001/2024", "Objeto teste",
                "2024-01-01 10:00:00", "Pregão", "FORNECEDOR TESTE", List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );

        PageResponseDTO<LicitacaoDTO> pageResponse = new PageResponseDTO<>(
                List.of(dto), 0, 20, 1, 1, true, true, false
        );

        when(service.findAll(isNull(), isNull(), eq(0), eq(20), isNull()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/licitacoes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listarLicitacoes_ComFiltros_DeveRetornarListaFiltrada() throws Exception {
        LicitacaoDTO dto = new LicitacaoDTO(
                1L, 123456, "001/2024", "Objeto teste",
                "2024-01-01 10:00:00", "Pregão", "FORNECEDOR TESTE", List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );

        PageResponseDTO<LicitacaoDTO> pageResponse = new PageResponseDTO<>(
                List.of(dto), 0, 20, 1, 1, true, true, false
        );

        when(service.findAll(eq(123456), eq("001/2024"), eq(0), eq(20), isNull()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/licitacoes")
                        .param("codigoUasg", "123456")
                        .param("numeroPregao", "001/2024")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigoUasg").value(123456))
                .andExpect(jsonPath("$.content[0].numeroPregao").value("001/2024"));
    }

    @Test
    void buscarPorId_DeveRetornarLicitacao() throws Exception {
        LicitacaoDTO dto = new LicitacaoDTO(
                1L, 123456, "001/2024", "Objeto teste",
                "2024-01-01 10:00:00", "Pregão", "FORNECEDOR TESTE", List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(service.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/licitacoes/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigoUasg").value(123456));
    }

    @Test
    void buscarPorId_LicitacaoNaoExiste_DeveRetornar404() throws Exception {
        when(service.findById(999L))
                .thenThrow(new ResourceNotFoundException("Licitação não encontrada"));

        mockMvc.perform(get("/api/licitacoes/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Licitação não encontrada"));
    }
}
