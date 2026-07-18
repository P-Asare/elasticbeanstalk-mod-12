package com.example.elasticbeanstalkmod12.service;

import com.example.elasticbeanstalkmod12.dto.CreateMessageRequest;
import com.example.elasticbeanstalkmod12.dto.MessageResponse;
import java.util.List;

public interface MessageService {
    MessageResponse create(CreateMessageRequest request);
    MessageResponse getById(String id);
    List<MessageResponse> getAll();
    List<MessageResponse> getByAuthor(String author);
    void delete(String id);
}
