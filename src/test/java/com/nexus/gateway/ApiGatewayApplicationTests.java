package com.nexus.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayApplicationTests {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.enabled", () -> "false");
    }

    @Test
    void contextLoads() {
    }
}
