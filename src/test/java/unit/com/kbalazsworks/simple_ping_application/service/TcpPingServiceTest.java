package unit.com.kbalazsworks.simple_ping_application.service;

import com.kbalazsworks.simple_ping_application.entities.PingResult;
import com.kbalazsworks.simple_ping_application.extensions.ApplicationProperties;
import com.kbalazsworks.simple_ping_application.providers.HttpClientProvider;
import com.kbalazsworks.simple_ping_application.repositories.TaskRunRepository;
import com.kbalazsworks.simple_ping_application.repositories.TcpPingRepository;
import com.kbalazsworks.simple_ping_application.services.ReportService;
import com.kbalazsworks.simple_ping_application.services.TcpPingService;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import unit.com.kbalazsworks.simple_ping_application.helpers.MockCreateHelper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static com.kbalazsworks.simple_ping_application.enums.RunTypeEnum.TCP_PING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TcpPingServiceTest
{
    private static final ApplicationProperties applicationProperties =
        MockCreateHelper.applicationProperties_default();

    @Test
    @SuppressWarnings("unchecked")
    public void ping_successfulPing_prefect() throws IOException, InterruptedException
    {
        // Arrange
        ApplicationProperties applicationProperties = MockCreateHelper.applicationProperties_default();
        String testedHost = "localhost";

        String expectedHost = "localhost";
        PingResult expectedResult = new PingResult(
            false,
            LocalDateTime.of(2020, 1, 2, 3, 4, 5),
            "Host: localhost, HTTP Status: 200, Response Time: 10ms"
        );

        ReportService reportServiceMock = MockCreateHelper.ReportService_default();

        HttpResponse<String> httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.statusCode()).thenReturn(200);

        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.send(eq(getExpectedRequest(expectedHost)), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        HttpClientProvider httpClientProviderMock = mock(HttpClientProvider.class);
        when(httpClientProviderMock.createClient()).thenReturn(httpClientMock);

        TcpPingRepository tcpPingRepository = new TcpPingRepository();
        TaskRunRepository taskRunRepositoryMock = mock(TaskRunRepository.class);
        when(taskRunRepositoryMock.isRunning(eq(TCP_PING), eq(expectedHost))).thenReturn(false);

        TcpPingService tcpPingService = new TcpPingService(
            reportServiceMock,
            tcpPingRepository,
            taskRunRepositoryMock,
            MockCreateHelper.LocalDateTimeProvider_now_default(),
            httpClientProviderMock,
            MockCreateHelper.DurationProvider_between_10ms(),
            applicationProperties
        );

        // Act
        try (LogCaptor logCaptor = LogCaptor.forClass(TcpPingService.class))
        {
            tcpPingService.ping(testedHost);

            // Assert
            assertAll(
                () -> assertThat(tcpPingRepository.get(expectedHost))
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> assertThat(logCaptor.getWarnLogs()).isEmpty(),
                () -> verify(reportServiceMock, never()).report(eq(expectedHost))
            );
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ping_runningPing_wontCreateNew() throws IOException, InterruptedException
    {
        // Arrange
        ApplicationProperties applicationProperties = MockCreateHelper.applicationProperties_default();
        String testedHost = "localhost";

        String expectedHost = "localhost";
        String expectedInfoLogStartWith = "Ping is already running";

        ReportService reportServiceMock = MockCreateHelper.ReportService_default();

        HttpResponse<String> httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.statusCode()).thenReturn(200);

        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.send(eq(getExpectedRequest(expectedHost)), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        HttpClientProvider httpClientProviderMock = mock(HttpClientProvider.class);
        when(httpClientProviderMock.createClient()).thenReturn(httpClientMock);

        TcpPingRepository tcpPingRepository = new TcpPingRepository();
        TaskRunRepository taskRunRepositoryMock = mock(TaskRunRepository.class);
        when(taskRunRepositoryMock.isRunning(eq(TCP_PING), eq(expectedHost))).thenReturn(true);

        TcpPingService tcpPingService = new TcpPingService(
            reportServiceMock,
            tcpPingRepository,
            taskRunRepositoryMock,
            MockCreateHelper.LocalDateTimeProvider_now_default(),
            httpClientProviderMock,
            MockCreateHelper.DurationProvider_between_10ms(),
            applicationProperties
        );

        // Act
        try (LogCaptor logCaptor = LogCaptor.forClass(TcpPingService.class))
        {
            tcpPingService.ping(testedHost);

            // Assert
            assertAll(
                () -> assertThat(logCaptor.getInfoLogs().get(1)).startsWith(expectedInfoLogStartWith),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> assertThat(logCaptor.getWarnLogs()).isEmpty(),
                () -> verify(httpClientMock, never()).send(any(), any())
            );
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ping_http404_callsErrorReporter() throws IOException, InterruptedException
    {
        // Arrange
        ApplicationProperties applicationProperties = MockCreateHelper.applicationProperties_default();
        String testedHost = "localhost";

        String expectedHost = "localhost";
        PingResult expectedResult = new PingResult(
            true,
            LocalDateTime.of(2020, 1, 2, 3, 4, 5),
            "Host: localhost, HTTP Status: 404, Response Time: 10ms"
        );

        ReportService reportServiceMock = MockCreateHelper.ReportService_default();

        HttpResponse<String> httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.statusCode()).thenReturn(404);

        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.send(eq(getExpectedRequest(expectedHost)), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        HttpClientProvider httpClientProviderMock = mock(HttpClientProvider.class);
        when(httpClientProviderMock.createClient()).thenReturn(httpClientMock);

        TcpPingRepository tcpPingRepository = new TcpPingRepository();

        TaskRunRepository taskRunRepositoryMock = mock(TaskRunRepository.class);
        when(taskRunRepositoryMock.isRunning(eq(TCP_PING), eq(expectedHost))).thenReturn(false);

        TcpPingService tcpPingService = new TcpPingService(
            reportServiceMock,
            tcpPingRepository,
            taskRunRepositoryMock,
            MockCreateHelper.LocalDateTimeProvider_now_default(),
            httpClientProviderMock,
            MockCreateHelper.DurationProvider_between_10ms(),
            applicationProperties
        );

        // Act
        try (LogCaptor logCaptor = LogCaptor.forClass(TcpPingService.class))
        {
            tcpPingService.ping(testedHost);

            // Assert
            assertAll(
                () -> assertThat(tcpPingRepository.get(expectedHost))
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> assertThat(logCaptor.getWarnLogs()).isNotEmpty(),
                () -> verify(reportServiceMock, only()).report(eq(expectedHost))
            );
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ping_slowRequest_callsErrorReporter() throws IOException, InterruptedException
    {
        // Arrange
        ApplicationProperties applicationProperties = MockCreateHelper.applicationProperties_default();
        String testedHost = "localhost";

        String expectedHost = "localhost";
        PingResult expectedResult = new PingResult(
            true,
            LocalDateTime.of(2020, 1, 2, 3, 4, 5),
            "Host: localhost, HTTP Status: 200, Response Time: 10000ms"
        );

        ReportService reportServiceMock = MockCreateHelper.ReportService_default();

        HttpResponse<String> httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.statusCode()).thenReturn(200);

        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.send(eq(getExpectedRequest(expectedHost)), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        HttpClientProvider httpClientProviderMock = mock(HttpClientProvider.class);
        when(httpClientProviderMock.createClient()).thenReturn(httpClientMock);

        TcpPingRepository tcpPingRepository = new TcpPingRepository();

        TaskRunRepository taskRunRepositoryMock = mock(TaskRunRepository.class);
        when(taskRunRepositoryMock.isRunning(eq(TCP_PING), eq(expectedHost))).thenReturn(false);

        TcpPingService tcpPingService = new TcpPingService(
            reportServiceMock,
            tcpPingRepository,
            taskRunRepositoryMock,
            MockCreateHelper.LocalDateTimeProvider_now_default(),
            httpClientProviderMock,
            MockCreateHelper.DurationProvider_between_returnsWith(10000),
            applicationProperties
        );

        // Act
        try (LogCaptor logCaptor = LogCaptor.forClass(TcpPingService.class))
        {
            tcpPingService.ping(testedHost);

            // Assert
            assertAll(
                () -> assertThat(tcpPingRepository.get(expectedHost))
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> assertThat(logCaptor.getWarnLogs()).isNotEmpty(),
                () -> verify(reportServiceMock, only()).report(eq(expectedHost))
            );
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ping_unknownError_callsErrorReporter() throws IOException, InterruptedException
    {
        // Arrange
        ApplicationProperties applicationProperties = MockCreateHelper.applicationProperties_default();
        String testedHost = "localhost";

        String expectedHost = "localhost";
        PingResult expectedResult = new PingResult(
            true,
            LocalDateTime.of(2020, 1, 2, 3, 4, 5),
            "Unknown error, check the app log."
        );

        ReportService reportServiceMock = MockCreateHelper.ReportService_default();

        HttpResponse<String> httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.statusCode()).thenReturn(200);

        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.send(any(), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new RuntimeException("error"));

        HttpClientProvider httpClientProviderMock = mock(HttpClientProvider.class);
        when(httpClientProviderMock.createClient()).thenReturn(httpClientMock);

        TcpPingRepository tcpPingRepository = new TcpPingRepository();

        TaskRunRepository taskRunRepositoryMock = mock(TaskRunRepository.class);
        when(taskRunRepositoryMock.isRunning(eq(TCP_PING), eq(expectedHost))).thenReturn(false);

        TcpPingService tcpPingService = new TcpPingService(
            reportServiceMock,
            tcpPingRepository,
            taskRunRepositoryMock,
            MockCreateHelper.LocalDateTimeProvider_now_default(),
            httpClientProviderMock,
            MockCreateHelper.DurationProvider_between_returnsWith(10000),
            applicationProperties
        );

        // Act
        try (LogCaptor logCaptor = LogCaptor.forClass(TcpPingService.class))
        {
            tcpPingService.ping(testedHost);

            // Assert
            assertAll(
                () -> assertThat(tcpPingRepository.get(expectedHost))
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult),
                () -> assertThat(logCaptor.getErrorLogs().getFirst()).startsWith("Failed to ping host:"),
                () -> assertThat(logCaptor.getWarnLogs()).isEmpty(),
                () -> verify(reportServiceMock, only()).report(eq(expectedHost))
            );
        }
    }

    private HttpRequest getExpectedRequest(String expectedHost)
    {
        URI expectedUri = URI.create(
            applicationProperties.getPingServiceTcpProtocol()
                + "://"
                + expectedHost
                + applicationProperties.getPingServiceTcpPingPortEndpoint()
        );

        return HttpRequest.newBuilder()
            .uri(expectedUri)
            .timeout(Duration.ofMillis(applicationProperties.getPingServiceTcpTimeout() + 1))
            .GET()
            .build();
    }
}
