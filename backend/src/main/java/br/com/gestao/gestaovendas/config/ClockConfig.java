package br.com.gestao.gestaovendas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Relógio no fuso do negócio (padrão America/Sao_Paulo). As datas são gravadas sem fuso
 * (TIMESTAMP), então "vendas de hoje" precisam fechar o dia no horário de Brasília mesmo
 * com o servidor em UTC. O fuso padrão da JVM é definido no main() (GestaoVendasApplication),
 * antes de qualquer outro código; os testes usam -Duser.timezone no surefire.
 */
@Configuration
public class ClockConfig {

    private final ZoneId fuso;

    public ClockConfig(@Value("${app.fuso-horario:America/Sao_Paulo}") String fuso) {
        this.fuso = ZoneId.of(fuso);
    }

    @Bean
    Clock clock() {
        return Clock.system(fuso);
    }
}
