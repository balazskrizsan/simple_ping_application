package com.kbalazsworks.simple_ping_application.repositories;

import com.kbalazsworks.simple_ping_application.entities.PingResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IcmpPingRepository
{
    private static final Map<String, PingResult> IN_MEMORY_STORE = new ConcurrentHashMap<>();

    public void save(String host, PingResult pingResult)
    {
        IN_MEMORY_STORE.put(host, pingResult);
    }

    public PingResult get(String host)
    {
        return IN_MEMORY_STORE.get(host);
    }
}
