package br.com.gestao.gestaovendas.common.evento;

/**
 * Fato que aconteceu no negócio (venda, perda, troca...). Publicado com o
 * ApplicationEventPublisher dentro da transação; quem escuta usa
 * {@code @TransactionalEventListener} para agir só depois do commit.
 * Os avisos por WhatsApp (futuro) vão assinar estes eventos.
 */
public interface EventoDominio {

    Long negocioId();
}
