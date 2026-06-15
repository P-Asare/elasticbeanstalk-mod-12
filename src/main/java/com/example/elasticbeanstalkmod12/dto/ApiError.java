package com.example.elasticbeanstalkmod12.dto;

import java.time.Instant;
import java.util.List;

public record ApiError(Instant timestamp, int status, String error, List<String> messages) {
}
