package com.kbalazsworkds.entities;

import lombok.NonNull;

public record Report(
    @NonNull String host,
    @NonNull String icmpPing
)
{
}
