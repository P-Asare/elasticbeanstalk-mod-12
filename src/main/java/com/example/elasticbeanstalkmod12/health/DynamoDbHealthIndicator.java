package com.example.elasticbeanstalkmod12.health;

import com.example.elasticbeanstalkmod12.config.DynamoDbProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;

@Component
public class DynamoDbHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbHealthIndicator.class);

    private static final String GENERIC_REASON = "DynamoDB connection unavailable";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbProperties properties;
    private final String appVersion;

    public DynamoDbHealthIndicator(DynamoDbClient dynamoDbClient,
                                   DynamoDbProperties properties,
                                   @Value("${app.version:unknown}") String appVersion) {
        this.dynamoDbClient = dynamoDbClient;
        this.properties = properties;
        this.appVersion = appVersion;
    }

    @Override
    public Health health() {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(properties.tableName())
                    .build());
            return Health.up().withDetail("version", appVersion).build();
        } catch (Exception ex) {
            log.warn("DynamoDB health check failed", ex);
            return Health.down()
                    .withDetail("version", appVersion)
                    .withDetail("reason", GENERIC_REASON)
                    .build();
        }
    }
}
