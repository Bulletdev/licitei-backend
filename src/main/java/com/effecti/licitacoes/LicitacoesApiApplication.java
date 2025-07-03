package com.effecti.licitacoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LicitacoesApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(LicitacoesApiApplication.class, args);
    }
}