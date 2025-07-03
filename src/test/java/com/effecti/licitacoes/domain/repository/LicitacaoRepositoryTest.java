package com.effecti.licitacoes.domain.repository;

import com.effecti.licitacoes.domain.entity.Licitacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class LicitacaoRepositoryTest {

    @Autowired
    private LicitacaoRepository repository;

    @Test
    void findByFilters_ComCodigoUasg_DeveRetornarLicitacoesFiltradas() {
        Licitacao licitacao1 = new Licitacao(123456, "001/2024", "Objeto 1", "2024-01-01 10:00:00", "PREGAO", "Endereço A");
        Licitacao licitacao2 = new Licitacao(789012, "002/2024", "Objeto 2", "2024-01-02 11:00:00", "DISPENSA", "Endereço B");

        repository.save(licitacao1);
        repository.save(licitacao2);

        Page<Licitacao> result = repository.findByFilters(123456, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCodigoUasg()).isEqualTo(123456);
    }

    @Test
    void findByFilters_ComNumeroPregao_DeveRetornarLicitacoesFiltradas() {
        Licitacao licitacao1 = new Licitacao(123456, "001/2024", "Objeto 1", "2024-01-01 10:00:00", "PREGAO", "Endereço A");
        Licitacao licitacao2 = new Licitacao(123456, "002/2024", "Objeto 2", "2024-01-02 11:00:00", "DISPENSA", "Endereço B");

        repository.save(licitacao1);
        repository.save(licitacao2);

        Page<Licitacao> result = repository.findByFilters(null, "001/2024", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNumeroPregao()).isEqualTo("001/2024");
    }

    @Test
    void existsByCodigoUasgAndNumeroPregao_LicitacaoExiste_DeveRetornarTrue() {
        Licitacao licitacao = new Licitacao(123456, "001/2024", "Objeto teste", "2024-01-01 10:00:00", "PREGAO", "Endereço teste");
        repository.save(licitacao);

        boolean exists = repository.existsByCodigoUasgAndNumeroPregao(123456, "001/2024");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByCodigoUasgAndNumeroPregao_LicitacaoNaoExiste_DeveRetornarFalse() {
        boolean exists = repository.existsByCodigoUasgAndNumeroPregao(999999, "999/2024");

        assertThat(exists).isFalse();
    }
}