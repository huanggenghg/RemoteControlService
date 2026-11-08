package com.lumostech.remotecontrol.controller;

import com.lumostech.remotecontrol.token.ZegoTokenResponse;
import com.lumostech.remotecontrol.token.ZegoTokenService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestController
public class RemoteControlController {
    private final ZegoTokenService tokens;
    public RemoteControlController(ZegoTokenService tokens) { this.tokens = tokens; }
    @RequestMapping("/world") public String hello() { return "Hello World"; }
    @PostMapping(value = "/getZegoToken", consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ZegoTokenResponse> token(@RequestBody UserInfo request) {
        ZegoTokenService.Issue issue = tokens.issue(request);
        return ResponseEntity.status(issue.status()).cacheControl(CacheControl.noStore())
            .body(issue.body());
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ZegoTokenResponse> malformed() {
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore())
            .body(ZegoTokenResponse.failure("INVALID_REQUEST", "请求格式无效"));
    }
}
