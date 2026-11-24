package br.com.gestao.gestaovendas.common.evento;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/** Por enquanto só registra no log. Exemplo de onde o WhatsApp vai se ligar. */
@Slf4j
@Component
public class EventosLogListener {

    @TransactionalEventListener
    public void aoConfirmar(EventoDominio evento) {
        log.info("Evento: {}", evento);
    }
}
