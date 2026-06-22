package com.example.elasticbeanstalkmod12.repository;

import com.example.elasticbeanstalkmod12.config.DynamoDbProperties;
import com.example.elasticbeanstalkmod12.domain.Message;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

@Repository
public class DynamoDbMessageRepository implements MessageRepository {

    private final DynamoDbClient client;
    private final String tableName;

    public DynamoDbMessageRepository(DynamoDbClient client, DynamoDbProperties props) {
        this.client = client;
        this.tableName = props.tableName();
    }

    @Override
    public Message save(Message message) {
        Map<String, AttributeValue> item = Map.of(
                "id", AttributeValue.fromS(message.id()),
                "author", AttributeValue.fromS(message.author()),
                "content", AttributeValue.fromS(message.content()),
                "createdAt", AttributeValue.fromS(message.createdAt().toString())
        );
        client.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build());
        return message;
    }

    @Override
    public Optional<Message> findById(String id) {
        var response = client.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("id", AttributeValue.fromS(id)))
                .build());
        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toMessage(response.item()));
    }

    @Override
    public void deleteById(String id) {
        client.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("id", AttributeValue.fromS(id)))
                .build());
    }

    @Override
    public List<Message> findAll() {
        return client.scanPaginator(ScanRequest.builder().tableName(tableName).build())
                .items()
                .stream()
                .map(this::toMessage)
                .toList();
    }

    private Message toMessage(Map<String, AttributeValue> item) {
        return new Message(
                getString(item, "id"),
                getString(item, "author"),
                getString(item, "content"),
                Instant.parse(getString(item, "createdAt"))
        );
    }

    private String getString(Map<String, AttributeValue> item, String key) {
        AttributeValue val = item.get(key);
        if (val == null) {
            throw new IllegalStateException("DynamoDB item missing required attribute: " + key);
        }
        return val.s();
    }
}
