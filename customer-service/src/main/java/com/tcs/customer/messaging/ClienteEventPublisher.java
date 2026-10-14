package com.tcs.customer.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publica el evento solo cuando la transacción de base de datos ya fue confirmada,
 * así nunca se anuncia un cambio que luego se revirtió.
 */
@Component
public class ClienteEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ClienteEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public ClienteEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicar(ClienteEvento evento) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, evento);
        } catch (Exception ex) {
            // El cliente ya quedó guardado; si el broker está caído se registra el fallo.
            // Mejora productiva: patrón Outbox para reintentar la publicación.
            log.error("No se pudo publicar el evento {} del cliente {}", evento.tipo(), evento.clienteId(), ex);
        }
    }
}
