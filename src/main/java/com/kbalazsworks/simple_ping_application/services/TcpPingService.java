package com.kbalazsworks.simple_ping_application.services;

import com.kbalazsworks.simple_ping_application.entities.PingResult;
import com.kbalazsworks.simple_ping_application.enums.RunTypeEnum;
import com.kbalazsworks.simple_ping_application.extensions.ApplicationProperties;
import com.kbalazsworks.simple_ping_application.providers.DurationProvider;
import com.kbalazsworks.simple_ping_application.providers.HttpClientProvider;
import com.kbalazsworks.simple_ping_application.providers.LocalDateTimeProvider;
import com.kbalazsworks.simple_ping_application.repositories.TaskRunRepository;
import com.kbalazsworks.simple_ping_application.repositories.TcpPingRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
@Log4j2
public class TcpPingService
{
    private final ReportService reportService;
    private final TcpPingRepository tcpPingRepository;
    private final TaskRunRepository taskRunRepository;
    private final LocalDateTimeProvider localDateTimeProvider;
    private final HttpClientProvider httpClientProvider;
    private final DurationProvider durationProvider;
    private final ApplicationProperties applicationProperties;

    public void ping(@NonNull String host)
    {
        log.info("Ping on host: {}", host);

        synchronized (taskRunRepository)
        {
            if (taskRunRepository.isRunning(RunTypeEnum.TCP_PING, host))
            {
                log.info("Ping is already running on host: {}", host);

                return;
            }

            taskRunRepository.setRunning(RunTypeEnum.TCP_PING, host);
        }

        Instant startTime = Instant.now();

        try (HttpClient client = httpClientProvider.createClient())
        {
            pingLogic(host, client, startTime);
        }
        catch (Exception e)
        {
            log.error("Failed to ping host: {}", host, e);

            tcpPingRepository.save(host, new PingResult(
                true,
                localDateTimeProvider.now(),
                "Unknown error, check the app log."
            ));

            reportService.report(host);
        }
        finally
        {
            taskRunRepository.finish(RunTypeEnum.TCP_PING, host);
        }
    }

    private void pingLogic(
        @NonNull String host,
        @NonNull HttpClient client,
        @NonNull Instant startTime
    ) throws IOException, InterruptedException
    {
        URI uri = URI.create(
            applicationProperties.getPingServiceTcpProtocol()
                + "://"
                + host
                + applicationProperties.getPingServiceTcpPingPortEndpoint()
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(Duration.ofMillis(applicationProperties.getPingServiceTcpTimeout() + 1))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        long duration = durationProvider.between(startTime, Instant.now()).toMillis();

        String lastResult = "Host: %s, HTTP Status: %d, Response Time: %sms".formatted(host, response.statusCode(), duration);
        log.info("Host: {}, HTTP Status: {}, Response Time: {}ms", host, response.statusCode(), duration);

        boolean hasError = hasError(response, duration);

        tcpPingRepository.save(host, new PingResult(
            hasError,
            localDateTimeProvider.now(),
            lastResult
        ));

        if (hasError)
        {
            log.warn("Ping error on host: {}", host);

            reportService.report(host);
        }

        log.info("Ping result: {}, {}", host, response);
    }

    private boolean hasError(@NonNull HttpResponse<String> response, long duration)
    {
        return response.statusCode() != 200 || duration > applicationProperties.getPingServiceTcpTimeout();
    }
}
