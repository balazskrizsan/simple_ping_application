package com.kbalazsworkds.services;

import com.kbalazsworkds.extensions.ApplicationProperties;
import com.kbalazsworkds.providers.DurationProvider;
import com.kbalazsworkds.providers.HttpClientProvider;
import com.kbalazsworkds.providers.LocalDateTimeProvider;
import com.kbalazsworkds.repositories.IcmpPingRepository;
import com.kbalazsworkds.repositories.TaskRunRepository;
import com.kbalazsworkds.repositories.TcpPingRepository;
import com.kbalazsworkds.repositories.TracerouteRepository;
import com.kbalazsworkds.tasks.IcmpPingTask;
import com.kbalazsworkds.tasks.TcpPingTask;
import com.kbalazsworkds.tasks.TracerouteTask;
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
    private final HttpClientProvider httpClientProvider = new HttpClientProvider();
    private final DurationProvider durationProvider = new DurationProvider();
    private final TcpPingRepository tcpPingRepository = new TcpPingRepository();
    private final IcmpPingRepository icmpPingRepository = new IcmpPingRepository();
    private final TracerouteRepository tracerouteRepository = new TracerouteRepository();
    private final TaskRunRepository taskRunRepository = new TaskRunRepository();

    private final ReportService reportService = new ReportService(
        httpClientProvider,
        applicationProperties,
        tcpPingRepository,
        icmpPingRepository,
        tracerouteRepository
    );

    private final IcmpPingService icmpPingService = new IcmpPingService(
        processRunService,
        reportService,
        icmpPingRepository,
        localDateTimeProvider,
        taskRunRepository
    );

    private final TcpPingService tcpPingService = new TcpPingService(
        reportService,
        tcpPingRepository,
        taskRunRepository,
        localDateTimeProvider,
        httpClientProvider,
        durationProvider,
        applicationProperties
    );

    private final TracerouteService tracerouteService = new TracerouteService(
        processRunService,
        tracerouteRepository,
        taskRunRepository,
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
