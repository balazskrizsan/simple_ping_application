package com.kbalazsworkds.repositories;

import com.kbalazsworkds.entities.PingResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TcpPingRepository
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
