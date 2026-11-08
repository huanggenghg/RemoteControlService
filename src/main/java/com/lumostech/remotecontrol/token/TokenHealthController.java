package com.lumostech.remotecontrol.token;

import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class TokenHealthController {
    private final ZegoTokenService tokens;
    public TokenHealthController(ZegoTokenService tokens) { this.tokens = tokens; }
    @GetMapping("/health") public ResponseEntity<Map<String, String>> health() {
        boolean ready = tokens.configured();
        return ResponseEntity.status(ready ? 200 : 503).cacheControl(CacheControl.noStore())
            .body(Map.of("status", ready ? "ok" : "not_configured"));
    }
}
