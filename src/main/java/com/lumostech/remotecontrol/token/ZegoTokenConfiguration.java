package com.lumostech.remotecontrol.token;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ZegoTokenConfiguration {
    @Bean public ZegoTokenService zegoTokenService(
        @Value("${ZEGO_APP_ID:0}") String appId,
        @Value("${ZEGO_SERVER_SECRET:}") String secret,
        @Value("${ZEGO_TOKEN_TTL_SECONDS:600}") String ttl) {
        return new ZegoTokenService(appId, secret, ttl);
    }
}
