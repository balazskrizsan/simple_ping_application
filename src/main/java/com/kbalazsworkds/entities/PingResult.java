package com.kbalazsworkds.entities;

import java.time.LocalDateTime;

public record PingResult(
    boolean hasError,
    LocalDateTime timestamp,
    String result
)
{
}
