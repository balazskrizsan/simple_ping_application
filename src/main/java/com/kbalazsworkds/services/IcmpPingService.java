package com.kbalazsworkds.services;

import com.kbalazsworkds.entities.PingResult;
import com.kbalazsworkds.entities.ProcessRunResponse;
import com.kbalazsworkds.providers.LocalDateTimeProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Log4j2
public class IcmpPingService
{
    private final ProcessRunService processRunService;
    private final ReportService reportService;
    private final LocalDateTimeProvider localDateTimeProvider;

    public static final Map<String, PingResult> LAST_ICMP_RESULTS = new ConcurrentHashMap<>();

    public void ping(@NonNull String host)
    {
        try
        {
            log.info("Ping on host: {}", host);

            ProcessRunResponse response = processRunService.run("ping", "-n", "5", host);
            boolean hasError = hasError(response);

            LAST_ICMP_RESULTS.put(host, new PingResult(
                hasError,
                localDateTimeProvider.now(),
                response.result())
            );

            if (hasError)
            {
                log.warn("Ping error on host: {}", host);

                reportService.report(host);
            }

            log.info("Ping result: {}, {}", host, response);
        }
        catch (Exception e)
        {
            log.error("Failed to ping host: {}", host, e);
            // @todo: set last result as unknown and call report
        }
    }

    private boolean hasError(@NonNull ProcessRunResponse response)
    {
        return response.exitCode() != 0
            || response.result().contains("Request timed out")
            || !response.result().contains("(0% loss)");
    }
}
