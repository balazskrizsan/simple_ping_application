package unit.com.kbalazsworkds.service;

import com.kbalazsworkds.entities.PingResult;
import com.kbalazsworkds.entities.ProcessRunResponse;
import com.kbalazsworkds.exceptions.ProcessRunException;
import com.kbalazsworkds.repositories.IcmpPingRepository;
import com.kbalazsworkds.repositories.TaskRunRepository;
import com.kbalazsworkds.services.IcmpPingService;
import com.kbalazsworkds.services.ProcessRunService;
import com.kbalazsworkds.services.ReportService;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import unit.com.kbalazsworkds.helpers.MockCreateHelper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IcmpPingServiceTest
{
    @Test
    public void ping_successfulPing_perfect() throws ProcessRunException
    {
        // Arrange
        String testedHost = "localhost.balazskrizsan.com";

        String expectedHost = "localhost.balazskrizsan.com";
        PingResult expectedResult = new PingResult(
            false,
            LocalDateTime.of(2020, 1, 2, 3, 4, 5),
            """
                Pinging localhost.balazskrizsan.com [127.0.0.1] with 32 bytes of data:
                Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
                Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
                Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
                Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
                Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
                                
                Ping statistics for 127.0.0.1:
                    Packets: Sent = 5, Received = 5, Lost = 0 (0% loss),
                Approximate round trip times in milli-seconds:
                    Minimum = 0ms, Maximum = 0ms, Average = 0ms"""
        );

        String mockedResponse = """
            Pinging localhost.balazskrizsan.com [127.0.0.1] with 32 bytes of data:
            Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
            Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
            Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
            Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
            Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
                        
            Ping statistics for 127.0.0.1:
                Packets: Sent = 5, Received = 5, Lost = 0 (0% loss),
            Approximate round trip times in milli-seconds:
                Minimum = 0ms, Maximum = 0ms, Average = 0ms""";

        ReportService reportServiceMock = MockCreateHelper.ReportService_default();
        IcmpPingRepository icmpPingRepository = new IcmpPingRepository();

        TaskRunRepository taskRunRepositoryMock = mock(TaskRunRepository.class);
        when(taskRunRepositoryMock.isRunning(any(), any())).thenReturn(false);

        IcmpPingService icmpPingService = new IcmpPingService(
            MockCreateHelper.ProcessRunService_run_ping(testedHost, new ProcessRunResponse(mockedResponse, 0)),
            reportServiceMock,
            icmpPingRepository,
            MockCreateHelper.LocalDateTimeProvider_now_default(),
            taskRunRepositoryMock
        );

        // Act - Assert
        try (LogCaptor logCaptor = LogCaptor.forClass(IcmpPingService.class))
        {
            icmpPingService.ping(testedHost);

            assertAll(
                () -> assertThat(icmpPingRepository.get(expectedHost))
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> assertThat(logCaptor.getWarnLogs()).isEmpty(),
                () -> verify(reportServiceMock, never()).report(anyString())
            );
        }
    }

    @Test
    public void ping_runningPing_wontStartNew() throws ProcessRunException
    {
        // Arrange
        String testedHost = "localhost.balazskrizsan.com";

        String mockedResponse = "ping";

        String exceptedLogStartsWith = "Ping is already running on host:";

        ReportService reportServiceMock = MockCreateHelper.ReportService_default();
        IcmpPingRepository icmpPingRepository = new IcmpPingRepository();

        TaskRunRepository taskRunRepositoryMock = mock(TaskRunRepository.class);
        when(taskRunRepositoryMock.isRunning(any(), any())).thenReturn(true);

        ProcessRunService processRunServiceMock = MockCreateHelper
            .ProcessRunService_run_ping(testedHost, new ProcessRunResponse(mockedResponse, 0));

        IcmpPingService icmpPingService = new IcmpPingService(
            processRunServiceMock,
            reportServiceMock,
            icmpPingRepository,
            MockCreateHelper.LocalDateTimeProvider_now_default(),
            taskRunRepositoryMock
        );

        // Act - Assert
        try (LogCaptor logCaptor = LogCaptor.forClass(IcmpPingService.class))
        {
            icmpPingService.ping(testedHost);

            assertAll(
                () -> assertThat(logCaptor.getInfoLogs().get(1)).startsWith(exceptedLogStartsWith),
                () -> verify(processRunServiceMock, never()).run(anyString())
            );
        }
    }

    @Test
    public void ping_20PercentsPacketlossPing_callsReporter() throws ProcessRunException
    {
        // Arrange
        String testedHost = "localhost.balazskrizsan.com";
        String expectedHost = "localhost.balazskrizsan.com";

        PingResult expectedResult = new PingResult(
            true,
            LocalDateTime.of(2020, 1, 2, 3, 4, 5),
            """
                Pinging localhost.balazskrizsan.com [127.0.0.1] with 32 bytes of data:
                Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
                Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
                Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
                Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
                Reply from 127.0.0.1: bytes=32 time<1ms TTL=128

                Ping statistics for 127.0.0.1:
                    Packets: Sent = 5, Received = 5, Lost = 0 (20% loss),
                Approximate round trip times in milli-seconds:
                    Minimum = 0ms, Maximum = 0ms, Average = 0ms"""
        );
        String expectedErrorMessage = "Ping error on host: " + expectedHost;

        String mockedResponse = """
            Pinging localhost.balazskrizsan.com [127.0.0.1] with 32 bytes of data:
            Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
            Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
            Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
            Reply from 127.0.0.1: bytes=32 time<1ms TTL=128
            Reply from 127.0.0.1: bytes=32 time<1ms TTL=128

            Ping statistics for 127.0.0.1:
                Packets: Sent = 5, Received = 5, Lost = 0 (20% loss),
            Approximate round trip times in milli-seconds:
                Minimum = 0ms, Maximum = 0ms, Average = 0ms""";

        ReportService reportServiceMock = MockCreateHelper.ReportService_default();

        IcmpPingRepository icmpPingRepository = new IcmpPingRepository();

        TaskRunRepository taskRunRepositoryMock = mock(TaskRunRepository.class);
        when(taskRunRepositoryMock.isRunning(any(), any())).thenReturn(false);

        IcmpPingService icmpPingService = new IcmpPingService(
            MockCreateHelper.ProcessRunService_run_ping(testedHost, new ProcessRunResponse(mockedResponse, 0)),
            reportServiceMock,
            icmpPingRepository,
            MockCreateHelper.LocalDateTimeProvider_now_default(),
            taskRunRepositoryMock
        );

        // Act - Assert
        try (LogCaptor logCaptor = LogCaptor.forClass(IcmpPingService.class))
        {
            icmpPingService.ping(testedHost);

            assertAll(
                () -> assertThat(icmpPingRepository.get(expectedHost))
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> assertThat(logCaptor.getWarnLogs()).containsExactly(expectedErrorMessage),
                () -> verify(reportServiceMock, only()).report(eq(expectedHost))
            );
        }
    }

    @Test
    public void ping_requestTimedOutPing_callsReporter() throws ProcessRunException
    {
        // Arrange
        String testedHost = "localhost.balazskrizsan.com";

        String expectedHost = "localhost.balazskrizsan.com";
        String expectedWarning = "Ping error on host: " + expectedHost;
        PingResult expectedResult = new PingResult(
            true,
            LocalDateTime.of(2020, 1, 2, 3, 4, 5),
            """
                Pinging host [192.168.1.1] with 32 bytes of data:
                Reply from 192.168.1.1: bytes=32 time=10ms TTL=64
                Reply from 192.168.1.1: bytes=32 time=12ms TTL=64
                Request timed out
                Reply from 192.168.1.1: bytes=32 time=11ms TTL=64
                Reply from 192.168.1.1: bytes=32 time=9ms TTL=64

                Ping statistics for 192.168.1.1:
                    Packets: Sent = 5, Received = 4, Lost = 1 (0% loss),
                Approximate round trip times in milli-seconds:
                    Minimum = 9ms, Maximum = 12ms, Average = 10ms"""
        );

        String mockedResponse = """
            Pinging host [192.168.1.1] with 32 bytes of data:
            Reply from 192.168.1.1: bytes=32 time=10ms TTL=64
            Reply from 192.168.1.1: bytes=32 time=12ms TTL=64
            Request timed out
            Reply from 192.168.1.1: bytes=32 time=11ms TTL=64
            Reply from 192.168.1.1: bytes=32 time=9ms TTL=64

            Ping statistics for 192.168.1.1:
                Packets: Sent = 5, Received = 4, Lost = 1 (0% loss),
            Approximate round trip times in milli-seconds:
                Minimum = 9ms, Maximum = 12ms, Average = 10ms""";

        ReportService reportServiceMock = MockCreateHelper.ReportService_default();

        IcmpPingRepository icmpPingRepository = new IcmpPingRepository();

        TaskRunRepository taskRunRepositoryMock = mock(TaskRunRepository.class);
        when(taskRunRepositoryMock.isRunning(any(), any())).thenReturn(false);

        IcmpPingService icmpPingService = new IcmpPingService(
            MockCreateHelper.ProcessRunService_run_ping(testedHost, new ProcessRunResponse(mockedResponse, 0)),
            reportServiceMock,
            icmpPingRepository,
            MockCreateHelper.LocalDateTimeProvider_now_default(),
            taskRunRepositoryMock
        );

        // Act - Assert
        try (LogCaptor logCaptor = LogCaptor.forClass(IcmpPingService.class))
        {
            icmpPingService.ping(testedHost);

            assertAll(
                () -> assertThat(icmpPingRepository.get(expectedHost))
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult),
                () -> assertThat(logCaptor.getWarnLogs()).containsExactly(expectedWarning),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> verify(reportServiceMock, only()).report(eq(expectedHost))
            );
        }
    }

    @Test
    public void ping_runWithExitCodeNotZero_callsReporter() throws ProcessRunException
    {
        // Arrange
        String testedHost = "localhost.balazskrizsan.com";

        String expectedHost = "localhost.balazskrizsan.com";
        String expectedWarning = "Ping error on host: " + expectedHost;
        PingResult expectedResult = new PingResult(
            true,
            LocalDateTime.of(2020, 1, 2, 3, 4, 5),
            """
                Pinging host [192.168.1.1] with 32 bytes of data:
                Reply from 192.168.1.1: bytes=32 time=10ms TTL=64
                Reply from 192.168.1.1: bytes=32 time=12ms TTL=64
                Reply from 192.168.1.1: bytes=32 time=12ms TTL=64
                Reply from 192.168.1.1: bytes=32 time=11ms TTL=64
                Reply from 192.168.1.1: bytes=32 time=9ms TTL=64

                Ping statistics for 192.168.1.1:
                    Packets: Sent = 5, Received = 4, Lost = 1 (0% loss),
                Approximate round trip times in milli-seconds:
                    Minimum = 9ms, Maximum = 12ms, Average = 10ms"""
        );

        String mockedResponse = """
            Pinging host [192.168.1.1] with 32 bytes of data:
            Reply from 192.168.1.1: bytes=32 time=10ms TTL=64
            Reply from 192.168.1.1: bytes=32 time=12ms TTL=64
            Reply from 192.168.1.1: bytes=32 time=12ms TTL=64
            Reply from 192.168.1.1: bytes=32 time=11ms TTL=64
            Reply from 192.168.1.1: bytes=32 time=9ms TTL=64

            Ping statistics for 192.168.1.1:
                Packets: Sent = 5, Received = 4, Lost = 1 (0% loss),
            Approximate round trip times in milli-seconds:
                Minimum = 9ms, Maximum = 12ms, Average = 10ms""";

        ReportService reportServiceMock = MockCreateHelper.ReportService_default();

        IcmpPingRepository icmpPingRepository = new IcmpPingRepository();

        TaskRunRepository taskRunRepositoryMock = mock(TaskRunRepository.class);
        when(taskRunRepositoryMock.isRunning(any(), any())).thenReturn(false);

        IcmpPingService icmpPingService = new IcmpPingService(
            MockCreateHelper.ProcessRunService_run_ping(testedHost, new ProcessRunResponse(mockedResponse, 1)),
            reportServiceMock,
            icmpPingRepository,
            MockCreateHelper.LocalDateTimeProvider_now_default(),
            taskRunRepositoryMock
        );

        // Act - Assert
        try (LogCaptor logCaptor = LogCaptor.forClass(IcmpPingService.class))
        {
            icmpPingService.ping(testedHost);

            assertAll(
                () -> assertThat(icmpPingRepository.get(expectedHost))
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult),
                () -> assertThat(logCaptor.getWarnLogs()).containsExactly(expectedWarning),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> verify(reportServiceMock, only()).report(eq(expectedHost))
            );
        }
    }
}
