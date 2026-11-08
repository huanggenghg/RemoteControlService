package com.lumostech.remotecontrol.token;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("token-only")
@SpringBootTest(classes = TokenRuntimeConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"server.address=127.0.0.1", "ZEGO_APP_ID=0", "ZEGO_SERVER_SECRET="})
class TokenRuntimeTest {
    @Autowired ApplicationContext context;
    @Autowired TestRestTemplate http;
    @Test void tokenOnlyRuntimeDoesNotStartUserDatabaseFeatures() {
        assertTrue(context.getBeansOfType(DataSource.class).isEmpty());
        assertFalse(context.containsBean("userAPI"));
        var response = http.getForEntity("/health", String.class);
        assertEquals(503, response.getStatusCode().value());
        assertTrue(response.getBody().contains("not_configured"));
    }
}
