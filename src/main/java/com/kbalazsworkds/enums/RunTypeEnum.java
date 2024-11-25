package com.kbalazsworkds.enums;

import lombok.Getter;

@Getter
public enum RunTypeEnum
{
    ICMP_PING(1),
    TCP_PING(2),
    TRACEROUTE(3);

    private final int value;

    RunTypeEnum(int value)
    {
        this.value = value;
    }
}
