package com.example.elasticbeanstalkmod12.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.elasticbeanstalkmod12.config.DynamoDbProperties;
import com.example.elasticbeanstalkmod12.domain.Message;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.paginators.ScanIterable;

@ExtendWith(MockitoExtension.class)
class DynamoDbMessageRepositoryTest {

    @Mock
    private DynamoDbClient client;

    private DynamoDbMessageRepository repository;

    private static final String TABLE = "messages";

    @BeforeEach
    void setUp() {
        DynamoDbProperties props = new DynamoDbProperties("eu-west-1", TABLE, null);
        repository = new DynamoDbMessageRepository(client, props);
    }

    @Test
    void save_putsItemAndReturnsMessage() {
        Instant now = Instant.now();
        Message message = new Message("id-1", "Alice", "Hello", now);

        ArgumentCaptor<PutItemRequest> captor = forClass(PutItemRequest.class);
        when(client.putItem(captor.capture())).thenReturn(null);

        Message result = repository.save(message);

        assertThat(result).isEqualTo(message);
        PutItemRequest req = captor.getValue();
        assertThat(req.tableName()).isEqualTo(TABLE);
        assertThat(req.item().get("id").s()).isEqualTo("id-1");
        assertThat(req.item().get("author").s()).isEqualTo("Alice");
        assertThat(req.item().get("content").s()).isEqualTo("Hello");
        assertThat(req.item().get("createdAt").s()).isEqualTo(now.toString());
    }

    @Test
    void findById_returnsMessage_whenItemExists() {
        Instant now = Instant.now();
        Map<String, AttributeValue> item = Map.of(
                "id", AttributeValue.fromS("id-2"),
                "author", AttributeValue.fromS("Bob"),
                "content", AttributeValue.fromS("World"),
                "createdAt", AttributeValue.fromS(now.toString())
        );
        GetItemResponse response = GetItemResponse.builder().item(item).build();
        when(client.getItem(any(GetItemRequest.class))).thenReturn(response);

        Optional<Message> result = repository.findById("id-2");

        assertThat(result).isPresent();
        Message msg = result.get();
        assertThat(msg.id()).isEqualTo("id-2");
        assertThat(msg.author()).isEqualTo("Bob");
        assertThat(msg.content()).isEqualTo("World");
        assertThat(msg.createdAt()).isEqualTo(now);
    }

    @Test
    void findById_returnsEmpty_whenNoItem() {
        GetItemResponse response = GetItemResponse.builder().build();
        when(client.getItem(any(GetItemRequest.class))).thenReturn(response);

        Optional<Message> result = repository.findById("missing");

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_returnsAllMessages() {
        Instant now = Instant.now();
        Map<String, AttributeValue> item1 = Map.of(
                "id", AttributeValue.fromS("id-1"),
                "author", AttributeValue.fromS("Alice"),
                "content", AttributeValue.fromS("Hello"),
                "createdAt", AttributeValue.fromS(now.toString())
        );
        Map<String, AttributeValue> item2 = Map.of(
                "id", AttributeValue.fromS("id-2"),
                "author", AttributeValue.fromS("Bob"),
                "content", AttributeValue.fromS("World"),
                "createdAt", AttributeValue.fromS(now.toString())
        );

        SdkIterable<Map<String, AttributeValue>> itemsIterable = () -> List.of(item1, item2).iterator();
        ScanIterable scanIterable = mock(ScanIterable.class);
        when(scanIterable.items()).thenReturn(itemsIterable);
        when(client.scanPaginator(any(ScanRequest.class))).thenReturn(scanIterable);

        List<Message> result = repository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("id-1");
        assertThat(result.get(1).id()).isEqualTo("id-2");
    }

    @Test
    void deleteById_sendsCorrectDeleteItemRequest() {
        ArgumentCaptor<DeleteItemRequest> captor = forClass(DeleteItemRequest.class);
        when(client.deleteItem(captor.capture())).thenReturn(DeleteItemResponse.builder().build());

        repository.deleteById("id-1");

        DeleteItemRequest req = captor.getValue();
        assertThat(req.tableName()).isEqualTo(TABLE);
        assertThat(req.key().get("id").s()).isEqualTo("id-1");
    }

    @Test
    void toMessage_throwsIllegalStateException_whenAttributeMissing() {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("author", AttributeValue.fromS("Alice"));
        item.put("content", AttributeValue.fromS("Hello"));
        item.put("createdAt", AttributeValue.fromS(Instant.now().toString()));

        GetItemResponse response = GetItemResponse.builder().item(item).build();
        when(client.getItem(any(GetItemRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> repository.findById("id-missing"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("id");
    }
}
