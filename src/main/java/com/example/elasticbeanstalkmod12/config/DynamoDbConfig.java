package com.example.elasticbeanstalkmod12.config;

import java.net.URI;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@EnableConfigurationProperties(DynamoDbProperties.class)
public class DynamoDbConfig {

    @Bean
    public DynamoDbClient dynamoDbClient(DynamoDbProperties props) {
        var builder = DynamoDbClient.builder()
                .region(Region.of(props.region()));
        if (props.endpoint() != null && !props.endpoint().isBlank()) {
            builder.endpointOverride(URI.create(props.endpoint()));
        }
        return builder.build();
    }
}
