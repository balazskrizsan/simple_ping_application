package com.kbalazsworkds.services;

import com.kbalazsworkds.entities.PingResult;
import com.kbalazsworkds.entities.ProcessRunResponse;
import com.kbalazsworkds.enums.RunTypeEnum;
import com.kbalazsworkds.providers.LocalDateTimeProvider;
import com.kbalazsworkds.repositories.IcmpPingRepository;
import com.kbalazsworkds.repositories.TaskRunRepository;
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
            // @todo: set last result as unknown and call report
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
