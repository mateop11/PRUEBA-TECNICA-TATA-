package com.tcs.account.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "cliente.exchange";
    public static final String ROUTING_KEY = "cliente.evento";
    public static final String QUEUE = "cuentas.cliente.queue";
    public static final String DLX = "cuentas.cliente.dlx";
    public static final String DLQ = "cuentas.cliente.queue.dlq";

    @Bean
    public TopicExchange clienteExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    /** Cola principal: si un mensaje falla tras los reintentos, pasa a la cola de errores (DLQ). */
    @Bean
    public Queue clienteQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue clienteDeadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding bindingCliente() {
        return BindingBuilder.bind(clienteQueue()).to(clienteExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding bindingDeadLetter() {
        return BindingBuilder.bind(clienteDeadLetterQueue()).to(deadLetterExchange()).with(DLQ);
    }

    /** Deserializa según el tipo del parámetro del listener, no según el nombre de clase del productor. */
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper mapper = new DefaultJackson2JavaTypeMapper();
        mapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        mapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(mapper);
        return converter;
    }
}
