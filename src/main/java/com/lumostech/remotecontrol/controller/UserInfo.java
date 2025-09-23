package com.lumostech.remotecontrol.controller;

public class UserInfo {
    private String userId;
    private String loginRoomId;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setLoginRoomId(String loginRoomId) {
        this.loginRoomId = loginRoomId;
    }

    public String getLoginRoomId() {
        return loginRoomId;
    }
}
