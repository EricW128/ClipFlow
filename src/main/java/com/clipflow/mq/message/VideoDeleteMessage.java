package com.clipflow.mq.message;

public class VideoDeleteMessage {

    private Long videoId;
    private String objectName;

    public VideoDeleteMessage() {
    }

    public VideoDeleteMessage(
            Long videoId,
            String objectName) {
        this.videoId = videoId;
        this.objectName = objectName;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }
}