package br.com.gestao.gestaovendas;

import java.time.ZoneId;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GestaoVendasApplication {

	public static void main(String[] args) {
		// Antes de tudo: LocalDateTime.now() em qualquer lugar usa o fuso do negócio.
		String fuso = System.getenv().getOrDefault("FUSO_HORARIO", "America/Sao_Paulo");
		TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(fuso)));
		SpringApplication.run(GestaoVendasApplication.class, args);
	}

}
