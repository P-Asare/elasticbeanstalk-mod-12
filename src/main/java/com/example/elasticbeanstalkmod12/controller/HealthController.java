package com.example.elasticbeanstalkmod12.controller;

import com.example.elasticbeanstalkmod12.health.DynamoDbHealthIndicator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private static final String GENERIC_REASON = "DynamoDB connection unavailable";

    private final DynamoDbHealthIndicator dynamoDbHealthIndicator;

    public HealthController(DynamoDbHealthIndicator dynamoDbHealthIndicator) {
        this.dynamoDbHealthIndicator = dynamoDbHealthIndicator;
    }

    @GetMapping("/health/db")
    public ResponseEntity<Map<String, Object>> dbHealth() {
        Health health = dynamoDbHealthIndicator.health();
        Map<String, Object> body = new LinkedHashMap<>();

        if (Status.UP.equals(health.getStatus())) {
            body.put("status", "UP");
            return ResponseEntity.ok(body);
        }

        body.put("status", "DOWN");
        Object reason = health.getDetails().get("reason");
        body.put("reason", reason != null ? reason.toString() : GENERIC_REASON);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
