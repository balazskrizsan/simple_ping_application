package com.kbalazsworkds.entities;

import java.time.LocalDateTime;

public record PingResult(
    boolean running,
    boolean hasError,
    LocalDateTime timestamp,
    String result
)
{
    public PingResult(boolean running)
    {
        this(running, false, null, null);
    }
}
