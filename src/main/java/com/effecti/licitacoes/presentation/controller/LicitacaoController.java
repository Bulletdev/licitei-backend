package com.effecti.licitacoes.presentation.controller;

import com.effecti.licitacoes.application.dto.LicitacaoDTO;
import com.effecti.licitacoes.application.dto.PageResponseDTO;
import com.effecti.licitacoes.application.service.LicitacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/licitacoes")
@Tag(name = "Licitações", description = "API para gerenciamento de licitações públicas")
@CrossOrigin(origins = "*")
public class LicitacaoController {

    private final LicitacaoService service;

    public LicitacaoController(LicitacaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar licitações", description = "Lista todas as licitações com filtros opcionais")
    public ResponseEntity<PageResponseDTO<LicitacaoDTO>> listarLicitacoes(
            @Parameter(description = "Código da UASG para filtro")
            @RequestParam(required = false) Integer codigoUasg,
            @Parameter(description = "Número do pregão para filtro")
            @RequestParam(required = false) String numeroPregao,
            @Parameter(description = "Número da página")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Ordenação (campo,direção)")
            @RequestParam(required = false) String sort
    ) {
        PageResponseDTO<LicitacaoDTO> licitacoes = service.findAll(
                codigoUasg, numeroPregao, page, size, sort
        );
        return ResponseEntity.ok(licitacoes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar licitação por ID", description = "Retorna uma licitação específica")
    public ResponseEntity<LicitacaoDTO> buscarPorId(
            @Parameter(description = "ID da licitação")
            @PathVariable Long id
    ) {
        LicitacaoDTO licitacao = service.findById(id);
        return ResponseEntity.ok(licitacao);
    }
}