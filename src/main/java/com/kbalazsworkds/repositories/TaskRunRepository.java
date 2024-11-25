package com.kbalazsworkds.repositories;

import com.kbalazsworkds.enums.RunTypeEnum;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskRunRepository
{
    private static final Map<String, List<Integer>> IN_MEMORY_STORE = new ConcurrentHashMap<>();

    public boolean isRunning(@NonNull RunTypeEnum runTypeEnum, @NonNull String host)
    {
        List<Integer> runList = IN_MEMORY_STORE.getOrDefault(host, new ArrayList<>());

        return runList.contains(runTypeEnum.getValue());
    }

    public void setRunning(@NonNull RunTypeEnum runTypeEnum, @NonNull String host)
    {
        List<Integer> runList = IN_MEMORY_STORE.getOrDefault(host, new ArrayList<>());

        runList.add(runTypeEnum.getValue());

        IN_MEMORY_STORE.put(host, runList);
    }

    public void finish(@NonNull RunTypeEnum runTypeEnum, @NonNull String host)
    {
        List<Integer> runList = IN_MEMORY_STORE.getOrDefault(host, new ArrayList<>());

        runList.remove(Integer.valueOf(runTypeEnum.getValue()));
    }
}
