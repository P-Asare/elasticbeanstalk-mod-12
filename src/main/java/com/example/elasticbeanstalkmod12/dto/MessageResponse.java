package com.example.elasticbeanstalkmod12.dto;

import java.time.Instant;

public record MessageResponse(String id, String author, String content, Instant createdAt) {
}
