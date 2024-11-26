package com.kbalazsworks.simple_ping_application.services;

import com.kbalazsworks.simple_ping_application.entities.PingResult;
import com.kbalazsworks.simple_ping_application.entities.ProcessRunResponse;
import com.kbalazsworks.simple_ping_application.enums.RunTypeEnum;
import com.kbalazsworks.simple_ping_application.providers.LocalDateTimeProvider;
import com.kbalazsworks.simple_ping_application.repositories.IcmpPingRepository;
import com.kbalazsworks.simple_ping_application.repositories.TaskRunRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class IcmpPingService
{
    private final ProcessRunService processRunService;
    private final ReportService reportService;
    private final IcmpPingRepository icmpPingRepository;
    private final LocalDateTimeProvider localDateTimeProvider;
    private final TaskRunRepository taskRunRepository;

    public void ping(@NonNull String host)
    {
        log.info("Ping on host: {}", host);

        synchronized (taskRunRepository)
        {
            if (taskRunRepository.isRunning(RunTypeEnum.ICMP_PING, host))
            {
                log.info("Ping is already running on host: {}", host);

                return;
            }

            taskRunRepository.setRunning(RunTypeEnum.ICMP_PING, host);
        }

        try
        {
            ProcessRunResponse response = processRunService.run("ping", "-n", "5", host);
            boolean hasError = hasError(response);

            icmpPingRepository.save(host, new PingResult(
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

            icmpPingRepository.save(host, new PingResult(
                true,
                localDateTimeProvider.now(),
                "Unknown error, check the app log."
            ));

            reportService.report(host);
        }
        finally
        {
            taskRunRepository.finish(RunTypeEnum.ICMP_PING, host);
        }
    }

    private boolean hasError(@NonNull ProcessRunResponse response)
    {
        return response.exitCode() != 0
            || response.result().contains("Request timed out")
            || !response.result().contains("(0% loss)");
    }
}
