package com.lumostech.remotecontrol.token;

import org.springframework.boot.builder.SpringApplicationBuilder;

public final class TokenServiceApplication {
    private TokenServiceApplication() {}
    public static void main(String[] args) {
        new SpringApplicationBuilder(TokenRuntimeConfiguration.class)
            .profiles("token-only")
            .properties("server.address=127.0.0.1", "server.port=8088",
                        "server.tomcat.threads.max=20", "server.tomcat.threads.min-spare=2")
            .run(args);
    }
}
