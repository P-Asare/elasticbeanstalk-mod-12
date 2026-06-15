package com.example.elasticbeanstalkmod12.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMessageRequest(
        @NotBlank String author,
        @NotBlank String content
) {
}
