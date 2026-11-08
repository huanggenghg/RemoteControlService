package com.lumostech.remotecontrol.token;

import com.lumostech.remotecontrol.controller.TokenServerAssistant;
import com.lumostech.remotecontrol.controller.UserInfo;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.springframework.http.HttpStatus;

public final class ZegoTokenService {
    @FunctionalInterface interface Issuer {
        String issue(long appId, String userId, String secret, int ttl) throws Exception;
    }
    public record Issue(HttpStatus status, ZegoTokenResponse body) {}
    private final long appId;
    private final String secret;
    private final int ttl;
    private final Issuer issuer;

    public ZegoTokenService(String appId, String secret, String ttl) {
        this(appId, secret, ttl, ZegoTokenService::generate);
    }
    ZegoTokenService(String appId, String secret, String ttl, Issuer issuer) {
        this.appId = parseLong(appId);
        this.secret = secret == null ? "" : secret;
        this.ttl = (int) Math.max(Integer.MIN_VALUE,
            Math.min(Integer.MAX_VALUE, parseLong(ttl)));
        this.issuer = issuer;
    }
    private static long parseLong(String value) {
        try { return Long.parseLong(value == null ? "" : value.trim()); }
        catch (NumberFormatException ignored) { return 0; }
    }
    public boolean configured() {
        return appId > 0 && appId <= 0xffff_ffffL &&
            secret.length() == 32 && secret.getBytes(StandardCharsets.UTF_8).length == 32 &&
            ttl >= 60 && ttl <= 86400;
    }
    public Issue issue(UserInfo request) {
        if (request == null || request.getUserId() == null ||
            request.getUserId().isBlank() || request.getUserId().length() > 64 ||
            request.getLoginRoomId() == null ||
            !request.getLoginRoomId().matches("[0-9]{6}")) {
            return fail(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "请求身份或协助码无效");
        }
        if (!configured()) {
            return fail(HttpStatus.SERVICE_UNAVAILABLE, "NOT_CONFIGURED", "凭证服务尚未配置");
        }
        try {
            String token = issuer.issue(appId, request.getUserId(), secret, ttl);
            if (token == null || !token.startsWith("04")) throw new IllegalStateException();
            byte[] wire = Base64.getDecoder().decode(token.substring(2));
            if (wire.length < 13 || wire[wire.length - 1] != 1) throw new IllegalStateException();
            long expiresAt = ByteBuffer.wrap(wire).getLong();
            if (expiresAt <= Instant.now().getEpochSecond()) throw new IllegalStateException();
            return new Issue(HttpStatus.OK, new ZegoTokenResponse(token,
                new ZegoTokenResponse.Error("SUCCESS", ""), Long.toString(appId),
                request.getLoginRoomId(), request.getUserId(), expiresAt));
        } catch (Exception ignored) {
            return fail(HttpStatus.INTERNAL_SERVER_ERROR, "SIGNING_FAILED", "凭证签发失败");
        }
    }
    private static String generate(long appId, String userId, String secret, int ttl) {
        TokenServerAssistant.TokenInfo info =
            TokenServerAssistant.generateToken04(appId, userId, secret, ttl, "");
        if (info.error == null || info.error.code != TokenServerAssistant.ErrorCode.SUCCESS) {
            throw new IllegalStateException("token_generation_failed");
        }
        return info.data;
    }
    private static Issue fail(HttpStatus status, String code, String message) {
        return new Issue(status, ZegoTokenResponse.failure(code, message));
    }
}
