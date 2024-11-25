package com.kbalazsworkds.providers;

import lombok.NonNull;

import java.time.Duration;
import java.time.Instant;

public class DurationProvider
{
    public Duration between(@NonNull Instant start, @NonNull Instant end)
    {
        return Duration.between(start, end);
    }
}
