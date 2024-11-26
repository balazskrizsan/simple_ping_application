package com.kbalazsworks.simple_ping_application.services;

import com.kbalazsworks.simple_ping_application.entities.PingResult;
import com.kbalazsworks.simple_ping_application.entities.ProcessRunResponse;
import com.kbalazsworks.simple_ping_application.enums.RunTypeEnum;
import com.kbalazsworks.simple_ping_application.providers.LocalDateTimeProvider;
import com.kbalazsworks.simple_ping_application.repositories.TaskRunRepository;
import com.kbalazsworks.simple_ping_application.repositories.TracerouteRepository;
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

        synchronized (taskRunRepository)
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
            String osName = System.getProperty("os.name").toLowerCase();
            String command = osName.contains("win") ? "tracert" : "traceroute";

            ProcessRunResponse response = processRunService.run(command, host);

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
