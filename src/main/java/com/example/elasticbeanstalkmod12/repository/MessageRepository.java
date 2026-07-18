package com.example.elasticbeanstalkmod12.repository;

import com.example.elasticbeanstalkmod12.domain.Message;
import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Message save(Message message);
    Optional<Message> findById(String id);
    List<Message> findAll();
    List<Message> findByAuthor(String author);
    void deleteById(String id);
}
