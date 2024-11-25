package com.kbalazsworkds.services;

import com.kbalazsworkds.entities.PingResult;
import com.kbalazsworkds.entities.ProcessRunResponse;
import com.kbalazsworkds.enums.RunTypeEnum;
import com.kbalazsworkds.providers.LocalDateTimeProvider;
import com.kbalazsworkds.repositories.TaskRunRepository;
import com.kbalazsworkds.repositories.TracerouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class TracerouteService
{
    private final ProcessRunService processRunService;
    private final TracerouteRepository tracerouteRepository;
    private final TaskRunRepository taskRunRepository;
    private final LocalDateTimeProvider localDateTimeProvider;

    public void trace(String host)
    {
        log.info("Tracing route: {}", host);

        synchronized (tracerouteRepository)
        {
            if (taskRunRepository.isRunning(RunTypeEnum.TRACEROUTE, host))
            {
                log.info("Traceroute already running on host: {}", host);

                return;
            }

            taskRunRepository.setRunning(RunTypeEnum.TRACEROUTE, host);
        }

        try
        {
            ProcessRunResponse response = processRunService.run("tracert", host);

            tracerouteRepository.save(host, new PingResult(
                false,
                localDateTimeProvider.now(),
                response.result()
            ));
        }
        catch (Exception e)
        {
            log.error("Failed to trace host: {}", host, e);

            tracerouteRepository.save(host, new PingResult(
                false,
                localDateTimeProvider.now(),
                "Unknown error, check the app log."
            ));
        }
        finally
        {
            taskRunRepository.finish(RunTypeEnum.TRACEROUTE, host);
        }
    }
}
