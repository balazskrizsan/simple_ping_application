package com.kbalazsworkds.tasks;

import com.kbalazsworkds.services.IcmpPingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class IcmpPingTask implements Runnable
{
    private final IcmpPingService icmpPingService;
    private final String host;

    @Override
    public void run()
    {
        log.info("Icmp ping task started");

        icmpPingService.ping(host);
    }
}
