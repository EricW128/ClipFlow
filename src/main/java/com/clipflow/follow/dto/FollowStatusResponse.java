package com.clipflow.follow.dto;

public class FollowStatusResponse {

    private boolean following;

    public FollowStatusResponse(boolean following) {
        this.following = following;
    }

    public boolean isFollowing() {
        return following;
    }
}