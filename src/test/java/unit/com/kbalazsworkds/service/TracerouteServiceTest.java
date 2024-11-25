package unit.com.kbalazsworkds.service;

import com.kbalazsworkds.entities.PingResult;
import com.kbalazsworkds.entities.ProcessRunResponse;
import com.kbalazsworkds.repositories.TaskRunRepository;
import com.kbalazsworkds.repositories.TracerouteRepository;
import com.kbalazsworkds.services.ProcessRunService;
import com.kbalazsworkds.services.ReportService;
import com.kbalazsworkds.services.TracerouteService;
import lombok.SneakyThrows;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import unit.com.kbalazsworkds.helpers.MockCreateHelper;

import java.time.LocalDateTime;

import static com.kbalazsworkds.enums.RunTypeEnum.TRACEROUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TracerouteServiceTest
{
    @Test
    @SneakyThrows
    public void test_runningTrace_wontStartNew()
    {
        // Arrange
        String testedHost = "localhost.balazskrizsan.com";
        TaskRunRepository taskRunRepository = new TaskRunRepository();

        taskRunRepository.setRunning(TRACEROUTE, testedHost);

        ProcessRunService processRunServiceMock = MockCreateHelper.ProcessRunService_run_tracert(
            testedHost,
            new ProcessRunResponse("", 0)
        );

        TracerouteService tracerouteService = new TracerouteService(
            processRunServiceMock,
            new TracerouteRepository(),
            taskRunRepository,
            MockCreateHelper.LocalDateTimeProvider_now_default()
        );

        // Act
        try (LogCaptor logCaptor = LogCaptor.forClass(ReportService.class))
        {
            tracerouteService.trace(testedHost);

            // Assert
            assertAll(
                () -> assertThat(logCaptor.getWarnLogs()).isEmpty(),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> verify(processRunServiceMock, never()).run(any(), any())
            );
        }
    }

    @Test
    @SneakyThrows
    public void test_newSuccessfulTask_savesTraceResultToRepository()
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
        try (LogCaptor logCaptor = LogCaptor.forClass(ReportService.class))
        {
            tracerouteService.trace(testedHost);

            // Assert
            assertAll(
                () -> assertThat(logCaptor.getWarnLogs()).isEmpty(),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> verify(processRunServiceMock, only())
                    .run(eq("tracert"), eq(expectedHost)),
                () -> assertThat(tracerouteRepository.get(expectedHost)).isEqualTo(expectedResult)
            );
        }
    }

    @Test
    @SneakyThrows
    public void test_inknownError_saveErrorLog()
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
                () -> verify(processRunServiceMock, only()).run(eq("tracert"), eq(expectedHost)),
                () -> assertThat(tracerouteRepository.get(expectedHost)).isEqualTo(expectedResult)
            );
        }
    }
}
