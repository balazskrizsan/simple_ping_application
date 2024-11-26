package com.kbalazsworkds;

import com.kbalazsworkds.extensions.ApplicationProperties;
import com.kbalazsworkds.providers.DurationProvider;
import com.kbalazsworkds.providers.HttpClientProvider;
import com.kbalazsworkds.providers.LocalDateTimeProvider;
import com.kbalazsworkds.repositories.IcmpPingRepository;
import com.kbalazsworkds.repositories.TaskRunRepository;
import com.kbalazsworkds.repositories.TcpPingRepository;
import com.kbalazsworkds.repositories.TracerouteRepository;
import com.kbalazsworkds.services.HostMonitorService;
import com.kbalazsworkds.services.IcmpPingService;
import com.kbalazsworkds.services.ProcessRunService;
import com.kbalazsworkds.services.ReportService;
import com.kbalazsworkds.services.TcpPingService;
import com.kbalazsworkds.services.TracerouteService;

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
