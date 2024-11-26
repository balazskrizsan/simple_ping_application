package com.kbalazsworks.simple_ping_application.services;

import com.kbalazsworks.simple_ping_application.extensions.ApplicationProperties;
import com.kbalazsworks.simple_ping_application.tasks.IcmpPingTask;
import com.kbalazsworks.simple_ping_application.tasks.TcpPingTask;
import com.kbalazsworks.simple_ping_application.tasks.TracerouteTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
@RequiredArgsConstructor
public class HostMonitorService
{
    private final ApplicationProperties applicationProperties;
    private final IcmpPingService icmpPingService;
    private final TcpPingService tcpPingService;
    private final TracerouteService tracerouteService;

    public void startMonitoring()
    {
        log.info("Starting monitoring on hosts: ");
        List<String> hosts = applicationProperties.getPingServiceHosts();
        hosts.forEach(log::info);

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(hosts.size());

        hosts.forEach(host -> {
            scheduler.scheduleWithFixedDelay(
                new IcmpPingTask(icmpPingService, host),
                0,
                applicationProperties.getPingServiceIcmpDelay(),
                TimeUnit.MILLISECONDS
            );

            scheduler.scheduleWithFixedDelay(
                new TcpPingTask(tcpPingService, host),
                0,
                applicationProperties.getPingServiceTcpDelay(),
                TimeUnit.MILLISECONDS
            );

            scheduler.scheduleWithFixedDelay(
                new TracerouteTask(tracerouteService, host),
                0,
                applicationProperties.getPingServiceTracerouteDelay(),
                TimeUnit.MILLISECONDS
            );
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();

            log.info("Shutting down Connection Monitor.");
        }));
    }
}
