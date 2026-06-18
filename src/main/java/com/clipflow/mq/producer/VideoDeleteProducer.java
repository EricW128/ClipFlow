package com.clipflow.mq.producer;

import com.clipflow.mq.config.RabbitMqConfig;
import com.clipflow.mq.message.VideoDeleteMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class VideoDeleteProducer {

    private final RabbitTemplate rabbitTemplate;

    public VideoDeleteProducer(
            RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(VideoDeleteMessage message) {

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.VIDEO_DELETE_EXCHANGE,
                RabbitMqConfig.VIDEO_DELETE_ROUTING_KEY,
                message
        );
    }
}