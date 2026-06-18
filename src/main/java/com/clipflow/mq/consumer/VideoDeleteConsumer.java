package com.clipflow.mq.consumer;

import com.clipflow.like.service.LikeService;
import com.clipflow.mq.config.RabbitMqConfig;
import com.clipflow.mq.message.VideoDeleteMessage;
import com.clipflow.storage.service.StorageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class VideoDeleteConsumer {

    private final StorageService storageService;
    private final LikeService likeService;

    public VideoDeleteConsumer(
            StorageService storageService,
            LikeService likeService) {
        this.storageService = storageService;
        this.likeService = likeService;
    }

    @RabbitListener(
            queues = RabbitMqConfig.VIDEO_DELETE_QUEUE
    )
    public void handleVideoDelete(
            VideoDeleteMessage message) {

        storageService.delete(
                message.getObjectName()
        );

        likeService.deleteVideoLikes(
                message.getVideoId()
        );
    }
}