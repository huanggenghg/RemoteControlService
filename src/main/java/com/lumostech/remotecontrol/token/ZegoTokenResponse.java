package com.lumostech.remotecontrol.token;

public record ZegoTokenResponse(String data, Error error, String appId,
                                String roomId, String userId, Long expiresAt) {
    public record Error(String code, String message) {}
    public static ZegoTokenResponse failure(String code, String message) {
        return new ZegoTokenResponse("", new Error(code, message), null, null, null, null);
    }
    @Override public String toString() {
        return "ZegoTokenResponse(code=" + error.code() + ", token=<redacted>)";
    }
}
