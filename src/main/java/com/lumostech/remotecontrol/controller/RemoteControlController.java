package com.lumostech.remotecontrol.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RemoteControlController {

    @RequestMapping("/world")
    public String hello() {
        return "Hello World";
    }

    @PostMapping("/getZegoToken")
    public String token(@RequestBody UserInfo userInfo) throws JsonProcessingException {
        long appId = 678281271L;    // 由即构提供
        String secretKey = "5ea6a10f0fbbab92bc786fcfa2fd9d45";  // 由即构提供
        String userId = userInfo.getUserId();    // 用户 ID，同一 appId 下全网唯一
        int effectiveTimeInSeconds = 60;   // 有效时间，单位：秒

        String payload = "{\"room_id\":\"" + userInfo.getLoginRoomId() + "\"}"; // 填入自定义的payload值，如room_id。 非必输，不传则payload赋值null。
        System.out.println("token: userId = " + userId);
        System.out.println("token: loginRoomId = " + userInfo.getLoginRoomId());
        TokenServerAssistant.VERBOSE = true;    // 正式运行时，最好置为 false
        TokenServerAssistant.TokenInfo token = TokenServerAssistant.generateToken04(appId, userId, secretKey, effectiveTimeInSeconds, payload);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(token);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<String> handleJsonProcessingException(JsonProcessingException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}
