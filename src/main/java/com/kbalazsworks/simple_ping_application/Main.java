package com.kbalazsworks.simple_ping_application;

import com.kbalazsworks.simple_ping_application.extensions.ApplicationProperties;
import com.kbalazsworks.simple_ping_application.providers.DurationProvider;
import com.kbalazsworks.simple_ping_application.providers.HttpClientProvider;
import com.kbalazsworks.simple_ping_application.providers.LocalDateTimeProvider;
import com.kbalazsworks.simple_ping_application.repositories.IcmpPingRepository;
import com.kbalazsworks.simple_ping_application.repositories.TaskRunRepository;
import com.kbalazsworks.simple_ping_application.repositories.TcpPingRepository;
import com.kbalazsworks.simple_ping_application.repositories.TracerouteRepository;
import com.kbalazsworks.simple_ping_application.services.HostMonitorService;
import com.kbalazsworks.simple_ping_application.services.IcmpPingService;
import com.kbalazsworks.simple_ping_application.services.ProcessRunService;
import com.kbalazsworks.simple_ping_application.services.ReportService;
import com.kbalazsworks.simple_ping_application.services.TcpPingService;
import com.kbalazsworks.simple_ping_application.services.TracerouteService;

public class Main
{
    private static final ApplicationProperties applicationProperties = new ApplicationProperties();

    private static final ProcessRunService processRunService = new ProcessRunService();
    private static final LocalDateTimeProvider localDateTimeProvider = new LocalDateTimeProvider();
    private static final HttpClientProvider httpClientProvider = new HttpClientProvider();
    private static final DurationProvider durationProvider = new DurationProvider();
    private static final TcpPingRepository tcpPingRepository = new TcpPingRepository();
    private static final IcmpPingRepository icmpPingRepository = new IcmpPingRepository();
    private static final TracerouteRepository tracerouteRepository = new TracerouteRepository();
    private static final TaskRunRepository taskRunRepository = new TaskRunRepository();

    private static final ReportService reportService = new ReportService(
        httpClientProvider,
        applicationProperties,
        tcpPingRepository,
        icmpPingRepository,
        tracerouteRepository
    );

    private static final IcmpPingService icmpPingService = new IcmpPingService(
        processRunService,
        reportService,
        icmpPingRepository,
        localDateTimeProvider,
        taskRunRepository
    );

    private static final TcpPingService tcpPingService = new TcpPingService(
        reportService,
        tcpPingRepository,
        taskRunRepository,
        localDateTimeProvider,
        httpClientProvider,
        durationProvider,
        applicationProperties
    );

    private static final TracerouteService tracerouteService = new TracerouteService(
        processRunService,
        tracerouteRepository,
        taskRunRepository,
        localDateTimeProvider
    );

    public static void main(String[] args)
    {
        HostMonitorService monitor = new HostMonitorService(
            applicationProperties,
            icmpPingService,
            tcpPingService,
            tracerouteService
        );
        monitor.startMonitoring();
    }
}
