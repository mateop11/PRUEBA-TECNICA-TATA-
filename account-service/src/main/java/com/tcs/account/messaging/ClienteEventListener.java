package com.tcs.account.messaging;

import com.tcs.account.domain.ClienteReplica;
import com.tcs.account.repository.ClienteReplicaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Consumidor idempotente: aplicar el mismo evento dos veces deja el mismo resultado. */
@Component
public class ClienteEventListener {

    private static final Logger log = LoggerFactory.getLogger(ClienteEventListener.class);

    private final ClienteReplicaRepository replicas;

    public ClienteEventListener(ClienteReplicaRepository replicas) {
        this.replicas = replicas;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    @Transactional
    public void alRecibirEvento(ClienteEvento evento) {
        log.info("Evento de cliente recibido: {} {}", evento.tipo(), evento.clienteId());
        if (ClienteEvento.ELIMINADO.equals(evento.tipo())) {
            // Las cuentas e historial se conservan; el cliente queda inactivo.
            replicas.findById(evento.clienteId()).ifPresent(r -> r.setEstado(false));
            return;
        }
        ClienteReplica replica = replicas.findById(evento.clienteId())
                .orElseGet(() -> new ClienteReplica(evento.clienteId(), evento.nombre(), evento.estado()));
        replica.setNombre(evento.nombre());
        replica.setEstado(evento.estado());
        replicas.save(replica);
    }
}
