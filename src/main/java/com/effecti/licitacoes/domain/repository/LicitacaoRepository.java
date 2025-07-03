package com.effecti.licitacoes.domain.repository;

import com.effecti.licitacoes.domain.entity.Licitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LicitacaoRepository extends JpaRepository<Licitacao, Long> {

    @Query("SELECT l FROM Licitacao l WHERE " +
            "(:codigoUasg IS NULL OR l.codigoUasg = :codigoUasg) AND " +
            "(:numeroPregao IS NULL OR l.numeroPregao = :numeroPregao)")
    Page<Licitacao> findByFilters(@Param("codigoUasg") Integer codigoUasg,
                                  @Param("numeroPregao") String numeroPregao,
                                  Pageable pageable);

    boolean existsByCodigoUasgAndNumeroPregao(Integer codigoUasg, String numeroPregao);
}