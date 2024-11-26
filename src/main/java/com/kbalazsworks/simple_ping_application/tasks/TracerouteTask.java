package com.kbalazsworks.simple_ping_application.tasks;

import com.kbalazsworks.simple_ping_application.services.TracerouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class TracerouteTask implements Runnable
{
    private final TracerouteService tracerouteService;
    private final String host;

    @Override
    public void run()
    {
        log.info("Trace task started");

        tracerouteService.trace(host);
    }
}
