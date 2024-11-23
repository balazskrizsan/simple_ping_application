package com.kbalazsworkds.services;

import com.kbalazsworkds.extensions.ApplicationProperties;
import com.kbalazsworkds.providers.LocalDateTimeProvider;
import com.kbalazsworkds.tasks.IcmpPingTask;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public class HostMonitorService
{
    private final ApplicationProperties applicationProperties = new ApplicationProperties();

    private final ProcessRunService processRunService = new ProcessRunService();
    private final LocalDateTimeProvider localDateTimeProvider = new LocalDateTimeProvider();

    private final ReportService reportService = new ReportService();
    private final IcmpPingService icmpPingService = new IcmpPingService(
        processRunService,
        reportService,
        localDateTimeProvider
    );

    public void startMonitoring()
    {
        log.info("Starting monitoring on hosts: ");
        List<String> hosts = applicationProperties.getPingServiceHosts();
        hosts.stream().map(String::trim).forEach(log::info);

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(hosts.size());

        applicationProperties.getPingServiceHosts().forEach(host -> {
            scheduler.scheduleWithFixedDelay(
                new IcmpPingTask(icmpPingService, host), 0, applicationProperties.getPingServiceIcmpDelay(), TimeUnit.MILLISECONDS
            );
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();

            log.info("Shutting down Connection Monitor.");
        }));
    }
}
