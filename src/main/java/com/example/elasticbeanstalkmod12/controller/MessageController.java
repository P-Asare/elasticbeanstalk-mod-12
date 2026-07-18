package com.example.elasticbeanstalkmod12.controller;

import com.example.elasticbeanstalkmod12.dto.CreateMessageRequest;
import com.example.elasticbeanstalkmod12.dto.MessageResponse;
import com.example.elasticbeanstalkmod12.service.MessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreateMessageRequest request) {
        MessageResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/messages/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public MessageResponse getById(@PathVariable String id) {
        return service.getById(id);
    }

    /**
     * List messages, optionally filtered by author.
     *
     * <p>
     * Matching is an exact, case-sensitive comparison against the stored
     * {@code author} attribute. A blank or absent {@code author} parameter
     * returns all messages. An empty result list is returned (never 404)
     * when no messages match the given author.
     *
     * @param author optional author filter; blank or absent returns all messages
     */
    @GetMapping
    public List<MessageResponse> getAll(
            @RequestParam(required = false) @Size(max = 100) String author) {
        return (author == null || author.isBlank())
                ? service.getAll()
                : service.getByAuthor(author);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
