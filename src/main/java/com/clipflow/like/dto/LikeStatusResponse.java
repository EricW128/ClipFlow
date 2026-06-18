package com.clipflow.like.dto;

public class LikeStatusResponse {

    private boolean liked;
    private long likeCount;

    public LikeStatusResponse(
            boolean liked,
            long likeCount) {
        this.liked = liked;
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public long getLikeCount() {
        return likeCount;
    }
}