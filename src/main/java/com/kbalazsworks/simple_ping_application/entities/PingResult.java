package com.kbalazsworks.simple_ping_application.entities;

import java.time.LocalDateTime;

public record PingResult(
    boolean hasError,
    LocalDateTime timestamp,
    String result
)
{
}
