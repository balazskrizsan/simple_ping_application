package unit.com.kbalazsworks.simple_ping_application.service;

import com.kbalazsworks.simple_ping_application.entities.PingResult;
import com.kbalazsworks.simple_ping_application.entities.ProcessRunResponse;
import com.kbalazsworks.simple_ping_application.exceptions.ProcessRunException;
import com.kbalazsworks.simple_ping_application.repositories.TaskRunRepository;
import com.kbalazsworks.simple_ping_application.repositories.TracerouteRepository;
import com.kbalazsworks.simple_ping_application.services.ProcessRunService;
import com.kbalazsworks.simple_ping_application.services.TracerouteService;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import unit.com.kbalazsworks.simple_ping_application.helpers.MockCreateHelper;

import java.time.LocalDateTime;

import static com.kbalazsworks.simple_ping_application.enums.RunTypeEnum.TRACEROUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TracerouteServiceTest
{
    @Test
    public void trace_runningTrace_wontStartNew() throws ProcessRunException
    {
        // Arrange
        String testedHost = "localhost.balazskrizsan.com";
        TaskRunRepository taskRunRepositoryMock = mock(TaskRunRepository.class);
        when(taskRunRepositoryMock.isRunning(eq(TRACEROUTE), eq(testedHost))).thenReturn(true);

        ProcessRunService processRunServiceMock = MockCreateHelper.ProcessRunService_run_tracert(
            testedHost,
            new ProcessRunResponse("", 0)
        );

        TracerouteService tracerouteService = new TracerouteService(
            processRunServiceMock,
            new TracerouteRepository(),
            taskRunRepositoryMock,
            MockCreateHelper.LocalDateTimeProvider_now_default()
        );

        // Act
        try (LogCaptor logCaptor = LogCaptor.forClass(TracerouteService.class))
        {
            tracerouteService.trace(testedHost);

            // Assert
            assertAll(
                () -> assertThat(logCaptor.getWarnLogs()).isEmpty(),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> verify(processRunServiceMock, never()).run(any(), any()),
                () -> verify(taskRunRepositoryMock, only()).isRunning(eq(TRACEROUTE), eq(testedHost)),
                () -> verify(taskRunRepositoryMock, never()).setRunning(any(), any())
            );
        }
    }

    @Test
    public void trace_newSuccessfulTask_savesTraceResultToRepository() throws ProcessRunException
    {
        // Arrange
        String testedHost = "localhost.balazskrizsan.com";

        String expectedHost = "localhost.balazskrizsan.com";
        PingResult expectedResult = new PingResult(
            false,
            LocalDateTime.of(2020, 1, 2, 3, 4, 5),
            "trace result"
        );

        TaskRunRepository taskRunRepository = new TaskRunRepository();
        TracerouteRepository tracerouteRepository = new TracerouteRepository();

        ProcessRunService processRunServiceMock = MockCreateHelper.ProcessRunService_run_tracert(
            testedHost,
            new ProcessRunResponse("trace result", 0)
        );

        TracerouteService tracerouteService = new TracerouteService(
            processRunServiceMock,
            tracerouteRepository,
            taskRunRepository,
            MockCreateHelper.LocalDateTimeProvider_now_default()
        );

        // Act
        try (LogCaptor logCaptor = LogCaptor.forClass(TracerouteService.class))
        {
            tracerouteService.trace(testedHost);

            // Assert
            assertAll(
                () -> assertThat(logCaptor.getWarnLogs()).isEmpty(),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> verify(processRunServiceMock, only()).run(
                    argThat(argument -> "tracert".equals(argument) || "traceroute".equals(argument)),
                    eq(expectedHost)
                ),
                () -> assertThat(tracerouteRepository.get(expectedHost)).isEqualTo(expectedResult)
            );
        }
    }

    @Test
    public void trace_unknownError_saveErrorLog() throws ProcessRunException
    {
        // Arrange
        String testedHost = "localhost.balazskrizsan.com";

        String expectedHost = "localhost.balazskrizsan.com";
        PingResult expectedResult = new PingResult(
            false,
            LocalDateTime.of(2020, 1, 2, 3, 4, 5),
            "Unknown error, check the app log."
        );

        TaskRunRepository taskRunRepository = new TaskRunRepository();
        TracerouteRepository tracerouteRepository = new TracerouteRepository();

        ProcessRunService processRunServiceMock = mock(ProcessRunService.class);
        when(processRunServiceMock.run(any(), any())).thenThrow(new RuntimeException("error"));

        TracerouteService tracerouteService = new TracerouteService(
            processRunServiceMock,
            tracerouteRepository,
            taskRunRepository,
            MockCreateHelper.LocalDateTimeProvider_now_default()
        );

        // Act
        try (LogCaptor logCaptor = LogCaptor.forClass(TracerouteService.class))
        {
            tracerouteService.trace(testedHost);

            // Assert
            assertAll(
                () -> assertThat(logCaptor.getWarnLogs()).isEmpty(),
                () -> assertThat(logCaptor.getErrorLogs().getFirst()).startsWith("Failed to trace host:"),
                () -> verify(processRunServiceMock, only()).run(
                    argThat(argument -> "tracert".equals(argument) || "traceroute".equals(argument)),
                    eq(expectedHost)
                ),
                () -> assertThat(tracerouteRepository.get(expectedHost)).isEqualTo(expectedResult)
            );
        }
    }
}
