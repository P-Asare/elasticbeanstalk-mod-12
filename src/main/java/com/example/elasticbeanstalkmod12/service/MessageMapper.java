package com.example.elasticbeanstalkmod12.service;

import com.example.elasticbeanstalkmod12.domain.Message;
import com.example.elasticbeanstalkmod12.dto.CreateMessageRequest;
import com.example.elasticbeanstalkmod12.dto.MessageResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public Message toNewMessage(CreateMessageRequest request) {
        return new Message(
                UUID.randomUUID().toString(),
                request.author(),
                request.content(),
                Instant.now()
        );
    }

    public MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.id(),
                message.author(),
                message.content(),
                message.createdAt()
        );
    }
}
