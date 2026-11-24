package br.com.gestao.gestaovendas;

import br.com.gestao.gestaovendas.support.IntegrationTest;
import org.junit.jupiter.api.Test;

/** Garante que as migrations batem com as entidades JPA (ddl-auto: validate). */
@IntegrationTest
class GestaoVendasApplicationIntegrationTest {

    @Test
    void contextoSobeComMigrationsValidas() {
    }
}
