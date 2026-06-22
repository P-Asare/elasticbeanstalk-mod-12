package com.example.elasticbeanstalkmod12.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.elasticbeanstalkmod12.domain.Message;
import com.example.elasticbeanstalkmod12.dto.CreateMessageRequest;
import com.example.elasticbeanstalkmod12.dto.MessageResponse;
import com.example.elasticbeanstalkmod12.exception.MessageNotFoundException;
import com.example.elasticbeanstalkmod12.repository.MessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository repository;

    private MessageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MessageServiceImpl(repository, new MessageMapper());
    }

    @Test
    void create_assignsUuidAndMapsFields() {
        CreateMessageRequest request = new CreateMessageRequest("Alice", "Hello");
        when(repository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        MessageResponse response = service.create(request);

        assertThat(response.id()).isNotBlank();
        assertThat(response.author()).isEqualTo("Alice");
        assertThat(response.content()).isEqualTo("Hello");
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void getById_returnsMappedResponse_whenFound() {
        Instant now = Instant.now();
        Message message = new Message("id-1", "Bob", "World", now);
        when(repository.findById("id-1")).thenReturn(Optional.of(message));

        MessageResponse response = service.getById("id-1");

        assertThat(response.id()).isEqualTo("id-1");
        assertThat(response.author()).isEqualTo("Bob");
        assertThat(response.content()).isEqualTo("World");
        assertThat(response.createdAt()).isEqualTo(now);
    }

    @Test
    void getById_throwsMessageNotFoundException_whenNotFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("missing"))
                .isInstanceOf(MessageNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void delete_callsRepository_whenFound() {
        Instant now = Instant.now();
        Message message = new Message("id-1", "Alice", "Hello", now);
        when(repository.findById("id-1")).thenReturn(Optional.of(message));

        service.delete("id-1");

        verify(repository).deleteById("id-1");
    }

    @Test
    void delete_throwsMessageNotFoundException_whenNotFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("missing"))
                .isInstanceOf(MessageNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void getAll_mapsAllMessages() {
        Instant now = Instant.now();
        List<Message> messages = List.of(
                new Message("id-1", "Alice", "Hello", now),
                new Message("id-2", "Bob", "World", now)
        );
        when(repository.findAll()).thenReturn(messages);

        List<MessageResponse> responses = service.getAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo("id-1");
        assertThat(responses.get(1).id()).isEqualTo("id-2");
    }
}
