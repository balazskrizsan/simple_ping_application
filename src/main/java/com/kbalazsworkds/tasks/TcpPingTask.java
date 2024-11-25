package com.kbalazsworkds.tasks;

import com.kbalazsworkds.services.TcpPingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class TcpPingTask implements Runnable
{
    private final TcpPingService tcpPingService;
    private final String host;

    @Override
    public void run()
    {
        log.info("Tcp ping task started");
        tcpPingService.ping(host);
    }
}
