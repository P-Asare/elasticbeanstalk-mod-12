package com.example.elasticbeanstalkmod12.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.elasticbeanstalkmod12.config.DynamoDbProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;

class DynamoDbHealthIndicatorTest {

    private DynamoDbClient dynamoDbClient;
    private DynamoDbHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        dynamoDbClient = mock(DynamoDbClient.class);
        DynamoDbProperties properties = new DynamoDbProperties("us-east-1", "messages", null);
        indicator = new DynamoDbHealthIndicator(dynamoDbClient, properties, "0.0.1-SNAPSHOT");
    }

    @Test
    void health_describeTableSucceeds_returnsUp() {
        when(dynamoDbClient.describeTable(any(DescribeTableRequest.class)))
                .thenReturn(DescribeTableResponse.builder().build());

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails().get("version")).isEqualTo("0.0.1-SNAPSHOT");
    }

    @Test
    void health_describeTableThrowsWithMessage_returnsDownWithGenericReason() {
        when(dynamoDbClient.describeTable(any(DescribeTableRequest.class)))
                .thenThrow(new RuntimeException("secret-endpoint-credentials-leak"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("reason")).isEqualTo("DynamoDB connection unavailable");
        assertThat(health.getDetails().get("reason")).isNotEqualTo("secret-endpoint-credentials-leak");
    }

    @Test
    void health_describeTableThrowsWithNullMessage_returnsDownWithGenericReason() {
        when(dynamoDbClient.describeTable(any(DescribeTableRequest.class)))
                .thenThrow(new RuntimeException((String) null));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("reason")).isEqualTo("DynamoDB connection unavailable");
    }
}
