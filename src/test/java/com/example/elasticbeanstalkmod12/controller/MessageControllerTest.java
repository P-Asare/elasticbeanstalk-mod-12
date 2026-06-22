package com.example.elasticbeanstalkmod12.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.elasticbeanstalkmod12.dto.CreateMessageRequest;
import com.example.elasticbeanstalkmod12.dto.MessageResponse;
import com.example.elasticbeanstalkmod12.exception.GlobalExceptionHandler;
import com.example.elasticbeanstalkmod12.exception.MessageNotFoundException;
import com.example.elasticbeanstalkmod12.service.MessageService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({MessageController.class, GlobalExceptionHandler.class})
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService service;

    @Test
    void postMessages_validBody_returns201WithBody() throws Exception {
        Instant now = Instant.now();
        MessageResponse response = new MessageResponse("uuid-1", "Alice", "Hello", now);
        when(service.create(any(CreateMessageRequest.class))).thenReturn(response);

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"author\":\"Alice\",\"content\":\"Hello\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/messages/uuid-1"))
                .andExpect(jsonPath("$.id").value("uuid-1"))
                .andExpect(jsonPath("$.author").value("Alice"))
                .andExpect(jsonPath("$.content").value("Hello"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void postMessages_blankAuthor_returns400WithErrorBody() throws Exception {
        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"author\":\"\",\"content\":\"Hello\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.messages").isArray());
    }

    @Test
    void getMessagesById_returns200WithBody() throws Exception {
        Instant now = Instant.now();
        MessageResponse response = new MessageResponse("uuid-1", "Alice", "Hello", now);
        when(service.getById("uuid-1")).thenReturn(response);

        mockMvc.perform(get("/messages/uuid-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("uuid-1"))
                .andExpect(jsonPath("$.author").value("Alice"))
                .andExpect(jsonPath("$.content").value("Hello"));
    }

    @Test
    void getMessagesById_notFound_returns404() throws Exception {
        when(service.getById("missing")).thenThrow(new MessageNotFoundException("missing"));

        mockMvc.perform(get("/messages/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void deleteMessagesById_returns204() throws Exception {
        mockMvc.perform(delete("/messages/uuid-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMessagesById_notFound_returns404() throws Exception {
        doThrow(new MessageNotFoundException("missing")).when(service).delete("missing");

        mockMvc.perform(delete("/messages/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getMessages_returns200WithArray() throws Exception {
        Instant now = Instant.now();
        List<MessageResponse> responses = List.of(
                new MessageResponse("uuid-1", "Alice", "Hello", now),
                new MessageResponse("uuid-2", "Bob", "World", now)
        );
        when(service.getAll()).thenReturn(responses);

        mockMvc.perform(get("/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("uuid-1"))
                .andExpect(jsonPath("$[1].id").value("uuid-2"));
    }
}
