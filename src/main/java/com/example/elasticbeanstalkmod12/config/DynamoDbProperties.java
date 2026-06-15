package com.example.elasticbeanstalkmod12.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.dynamodb")
public record DynamoDbProperties(
        @NotBlank String region,
        @NotBlank String tableName,
        String endpoint
) {
}
