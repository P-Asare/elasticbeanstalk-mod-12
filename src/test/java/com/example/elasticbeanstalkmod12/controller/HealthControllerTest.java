package com.example.elasticbeanstalkmod12.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.elasticbeanstalkmod12.health.DynamoDbHealthIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DynamoDbHealthIndicator indicator;

    @Test
    void getHealthDb_up_returns200() throws Exception {
        when(indicator.health()).thenReturn(Health.up().build());

        mockMvc.perform(get("/health/db"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.reason").doesNotExist());
    }

    @Test
    void getHealthDb_downWithReason_returns503WithGenericReason() throws Exception {
        when(indicator.health()).thenReturn(
                Health.down().withDetail("reason", "DynamoDB connection unavailable").build());

        mockMvc.perform(get("/health/db"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.reason").value("DynamoDB connection unavailable"));
    }

    @Test
    void getHealthDb_downWithoutReasonDetail_returns503WithGenericReason() throws Exception {
        when(indicator.health()).thenReturn(Health.down().build());

        mockMvc.perform(get("/health/db"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.reason").value("DynamoDB connection unavailable"));
    }

    @Test
    void getHealthDb_outOfService_returns503Down() throws Exception {
        when(indicator.health()).thenReturn(Health.status(Status.OUT_OF_SERVICE).build());

        mockMvc.perform(get("/health/db"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"));
    }
}
