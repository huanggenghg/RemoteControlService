package com.lumostech.remotecontrol.token;

import com.lumostech.remotecontrol.controller.RemoteControlController;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TokenControllerTest {
    @Test void preservesLegacyFieldsAndAddsCredentialMetadata() throws Exception {
        var key = UUID.randomUUID().toString().replace("-", "");
        var mvc = MockMvcBuilders.standaloneSetup(new RemoteControlController(
            new ZegoTokenService("42", key, "600"))).build();
        mvc.perform(post("/getZegoToken").contentType("application/json")
            .content("{\"userId\":\"test-user\",\"loginRoomId\":\"123456\"}"))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(jsonPath("$.data").isNotEmpty()).andExpect(jsonPath("$.error.code").value("SUCCESS"))
            .andExpect(jsonPath("$.appId").value("42")).andExpect(jsonPath("$.roomId").value("123456"))
            .andExpect(jsonPath("$.userId").value("test-user")).andExpect(jsonPath("$.expiresAt").isNumber());
    }
    @Test void malformedJsonHasSafe400Response() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new RemoteControlController(
            new ZegoTokenService("", "", "600"))).build();
        mvc.perform(post("/getZegoToken").contentType("application/json").content("{"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }
}
