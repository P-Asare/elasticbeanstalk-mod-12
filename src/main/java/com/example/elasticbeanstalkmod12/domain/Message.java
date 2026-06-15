package com.example.elasticbeanstalkmod12.domain;

import java.time.Instant;

public record Message(String id, String author, String content, Instant createdAt) {
}
