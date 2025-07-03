package com.effecti.licitacoes.infrastructure.service;

import com.effecti.licitacoes.domain.repository.LicitacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComprasNetScrapingServiceTest {

    @Mock
    private LicitacaoRepository repository;

    @InjectMocks
    private ComprasNetScrapingService service;

    @BeforeEach
    void setup() {
        Logger serviceLogger = (Logger) LoggerFactory.getLogger(ComprasNetScrapingService.class.getPackage().getName());
        serviceLogger.setLevel(Level.TRACE);
    }

    @Test
    void capturarLicitacoes_DeveProcessarSemErros() {
        when(repository.existsByCodigoUasgAndNumeroPregao(anyInt(), anyString()))
                .thenReturn(false);
        when(repository.saveAll(anyList())).thenReturn(List.of());

        // verifica se o método executa sem lançar exceções
        service.capturarLicitacoes();

        verify(repository, atLeastOnce()).existsByCodigoUasgAndNumeroPregao(anyInt(), anyString());
    }
}
