package com.lumostech.remotecontrol.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumostech.remotecontrol.controller.UserInfo;
import com.lumostech.remotecontrol.controller.TokenServerAssistant;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

class ZegoTokenServiceTest {
    private static UserInfo request(String user, String room) {
        UserInfo request = new UserInfo();
        request.setUserId(user); request.setLoginRoomId(room); return request;
    }
    private static String secret() { return UUID.randomUUID().toString().replace("-", ""); }
    @Test void generatedTokenHasExpectedIdentityAndCanBeDecrypted() throws Exception {
        String key = secret();
        var result = new ZegoTokenService("42", key, "600").issue(request("test-user", "123456"));
        assertEquals(HttpStatus.OK, result.status());
        var response = result.body();
        assertEquals("SUCCESS", response.error().code());
        assertEquals("42", response.appId());
        assertEquals("test-user", response.userId());
        assertEquals("123456", response.roomId());
        byte[] wire = Base64.getDecoder().decode(response.data().substring(2));
        ByteBuffer buffer = ByteBuffer.wrap(wire);
        assertEquals(response.expiresAt().longValue(), buffer.getLong());
        int ivLength = Short.toUnsignedInt(buffer.getShort());
        assertEquals(12, ivLength);
        byte[] iv = new byte[ivLength]; buffer.get(iv);
        int encryptedLength = Short.toUnsignedInt(buffer.getShort());
        byte[] encrypted = new byte[encryptedLength]; buffer.get(encrypted);
        assertEquals(1, buffer.get()); assertEquals(0, buffer.remaining());
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"),
            new GCMParameterSpec(128, iv));
        JsonNode content = new ObjectMapper().readTree(cipher.doFinal(encrypted));
        assertEquals(42L, content.get("app_id").asLong());
        assertEquals("test-user", content.get("user_id").asText());
        assertEquals(600L, content.get("expire").asLong() - content.get("ctime").asLong());
        assertEquals("", content.get("payload").asText());
        assertFalse(content.has("id"));
        assertFalse(content.has("name"));
        assertFalse(content.has("age"));
        var raw = TokenServerAssistant.generateToken04(42, "test-user", key, 600, "");
        assertFalse(raw.toString().contains(raw.data));
        assertFalse(response.toString().contains(response.data()));
        assertFalse(response.toString().contains(key));
    }
    @Test void missingOrInvalidConfigurationCannotSign() {
        for (var service : new ZegoTokenService[] {
            new ZegoTokenService("", "", "600"),
            new ZegoTokenService("4294967296", secret(), "600"),
            new ZegoTokenService("42", secret(), "0"),
            new ZegoTokenService("42", "short", "600"),
            new ZegoTokenService("42", "é".repeat(32), "600"),
            new ZegoTokenService("42", secret(), "86401"),
            new ZegoTokenService("42", secret(), "not-a-number")}) {
            var result = service.issue(request("test-user", "123456"));
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.status());
            assertEquals("NOT_CONFIGURED", result.body().error().code());
            assertEquals("", result.body().data());
        }
    }
    @Test void invalidRequestCannotReachIssuer() {
        var service = new ZegoTokenService("42", secret(), "600",
            (app, user, key, ttl) -> { fail("issuer must not run"); return ""; });
        for (UserInfo request : new UserInfo[] {null, request("", "123456"),
            request("x".repeat(65), "123456"), request("user", "abc123"), request("user", "１２３４５６")}) {
            assertEquals(HttpStatus.BAD_REQUEST, service.issue(request).status());
        }
    }
    @Test void internalFailureDoesNotLeakItsMessage() {
        var service = new ZegoTokenService("42", secret(), "600",
            (app, user, key, ttl) -> { throw new IllegalStateException("do-not-expose"); });
        var result = service.issue(request("test-user", "123456"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.status());
        assertEquals("SIGNING_FAILED", result.body().error().code());
        assertFalse(result.body().error().message().contains("do-not-expose"));
    }
}
