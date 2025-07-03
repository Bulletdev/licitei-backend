package com.effecti.licitacoes.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "licitacoes", indexes = {
        @Index(name = "idx_uasg", columnList = "codigoUasg"),
        @Index(name = "idx_pregao", columnList = "numeroPregao")
})
public class Licitacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Integer codigoUasg;

    @NotBlank
    @Column(nullable = false)
    private String numeroPregao;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String objeto;

    @Column
    private String dataAbertura;

    @NotBlank
    @Column(nullable = false)
    private String modalidade;

    @Column
    private String endereco;

    @OneToMany(mappedBy = "licitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemLicitacao> itens = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    public Licitacao() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    public Licitacao(Integer codigoUasg, String numeroPregao, String objeto,
                     String dataAbertura, String modalidade, String endereco) {
        this();
        this.codigoUasg = codigoUasg;
        this.numeroPregao = numeroPregao;
        this.objeto = objeto;
        this.dataAbertura = dataAbertura;
        this.modalidade = modalidade;
        this.endereco = endereco;
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCodigoUasg() {
        return codigoUasg;
    }

    public void setCodigoUasg(Integer codigoUasg) {
        this.codigoUasg = codigoUasg;
    }

    public String getNumeroPregao() {
        return numeroPregao;
    }

    public void setNumeroPregao(String numeroPregao) {
        this.numeroPregao = numeroPregao;
    }

    public String getObjeto() {
        return objeto;
    }

    public void setObjeto(String objeto) {
        this.objeto = objeto;
    }

    public String getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(String dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public List<ItemLicitacao> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public void setItens(List<ItemLicitacao> itens) {
        this.itens = new ArrayList<>(itens);
    }

    public void addItem(ItemLicitacao item) {
        if (item != null) {
            this.itens.add(item);
            item.setLicitacao(this);
        }
    }

    public void removeItem(ItemLicitacao item) {
        if (item != null && this.itens.remove(item)) {
            item.setLicitacao(null);
        }
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Licitacao licitacao = (Licitacao) o;
        return Objects.equals(id, licitacao.id) &&
               Objects.equals(codigoUasg, licitacao.codigoUasg) &&
               Objects.equals(numeroPregao, licitacao.numeroPregao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codigoUasg, numeroPregao);
    }

    @Override
    public String toString() {
        return "Licitacao{" +
                "id=" + id +
                ", codigoUasg=" + codigoUasg +
                ", numeroPregao='" + numeroPregao + '\'' +
                ", objeto='" + objeto + '\'' +
                '}';
    }
}