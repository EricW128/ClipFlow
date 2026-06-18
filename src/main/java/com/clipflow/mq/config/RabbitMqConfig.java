package com.clipflow.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMqConfig {

    public static final String VIDEO_DELETE_EXCHANGE =
            "video.delete.exchange";

    public static final String VIDEO_DELETE_QUEUE =
            "video.delete.queue";

    public static final String VIDEO_DELETE_ROUTING_KEY =
            "video.delete";
    public static final String VIDEO_DELETE_DEAD_EXCHANGE =
            "video.delete.dead.exchange";

    public static final String VIDEO_DELETE_DEAD_QUEUE =
            "video.delete.dead.queue";

    public static final String VIDEO_DELETE_DEAD_ROUTING_KEY =
            "video.delete.dead";

    @Bean
    public Queue videoDeleteQueue() {
        return QueueBuilder
                .durable(VIDEO_DELETE_QUEUE)
                .deadLetterExchange(
                        VIDEO_DELETE_DEAD_EXCHANGE
                )
                .deadLetterRoutingKey(
                        VIDEO_DELETE_DEAD_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public DirectExchange videoDeleteExchange() {
        return new DirectExchange(
                VIDEO_DELETE_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Binding videoDeleteBinding(
            @Qualifier("videoDeleteQueue")
            Queue videoDeleteQueue,

            @Qualifier("videoDeleteExchange")
            DirectExchange videoDeleteExchange) {

        return BindingBuilder
                .bind(videoDeleteQueue)
                .to(videoDeleteExchange)
                .with(VIDEO_DELETE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue videoDeleteDeadQueue() {
        return new Queue(
                VIDEO_DELETE_DEAD_QUEUE,
                true
        );
    }

    @Bean
    public DirectExchange videoDeleteDeadExchange() {
        return new DirectExchange(
                VIDEO_DELETE_DEAD_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Binding videoDeleteDeadBinding(
            @Qualifier("videoDeleteDeadQueue")
            Queue videoDeleteDeadQueue,

            @Qualifier("videoDeleteDeadExchange")
            DirectExchange videoDeleteDeadExchange) {

        return BindingBuilder
                .bind(videoDeleteDeadQueue)
                .to(videoDeleteDeadExchange)
                .with(VIDEO_DELETE_DEAD_ROUTING_KEY);
    }

}