package com.example.elasticbeanstalkmod12.service;

import com.example.elasticbeanstalkmod12.domain.Message;
import com.example.elasticbeanstalkmod12.dto.CreateMessageRequest;
import com.example.elasticbeanstalkmod12.dto.MessageResponse;
import com.example.elasticbeanstalkmod12.exception.MessageNotFoundException;
import com.example.elasticbeanstalkmod12.repository.MessageRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository repository;
    private final MessageMapper mapper;

    public MessageServiceImpl(MessageRepository repository, MessageMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public MessageResponse create(CreateMessageRequest request) {
        Message saved = repository.save(mapper.toNewMessage(request));
        return mapper.toResponse(saved);
    }

    @Override
    public MessageResponse getById(String id) {
        Message message = repository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));
        return mapper.toResponse(message);
    }

    @Override
    public List<MessageResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }
}
