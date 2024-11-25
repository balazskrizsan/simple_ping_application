package unit.com.kbalazsworkds.service;

import com.kbalazsworkds.entities.PingResult;
import com.kbalazsworkds.extensions.ApplicationProperties;
import com.kbalazsworkds.providers.HttpClientProvider;
import com.kbalazsworkds.services.ReportService;
import com.kbalazsworkds.services.TcpPingService;
import lombok.SneakyThrows;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import unit.com.kbalazsworkds.helpers.MockCreateHelper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static com.kbalazsworkds.services.TcpPingService.LAST_TCP_RESULTS;
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
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void ping_successfulPing_prefect()
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

        TcpPingService tcpPingService = new TcpPingService(
            reportServiceMock,
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
                () -> assertThat(LAST_TCP_RESULTS.get(expectedHost))
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> assertThat(logCaptor.getWarnLogs()).isEmpty(),
                () -> verify(reportServiceMock, never()).report(eq(expectedHost))
            );
        }
    }

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void ping_http404_callsErrorReporter()
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

        TcpPingService tcpPingService = new TcpPingService(
            reportServiceMock,
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
                () -> assertThat(LAST_TCP_RESULTS.get(expectedHost))
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> assertThat(logCaptor.getWarnLogs()).isNotEmpty(),
                () -> verify(reportServiceMock, only()).report(eq(expectedHost))
            );
        }
    }

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void ping_slowRequest_callsErrorReporter()
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

        TcpPingService tcpPingService = new TcpPingService(
            reportServiceMock,
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
                () -> assertThat(LAST_TCP_RESULTS.get(expectedHost))
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult),
                () -> assertThat(logCaptor.getErrorLogs()).isEmpty(),
                () -> assertThat(logCaptor.getWarnLogs()).isNotEmpty(),
                () -> verify(reportServiceMock, only()).report(eq(expectedHost))
            );
        }
    }

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void ping_unknownError_callsErrorReporter()
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

        TcpPingService tcpPingService = new TcpPingService(
            reportServiceMock,
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
                () -> assertThat(LAST_TCP_RESULTS.get(expectedHost))
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult),
                () -> assertThat(logCaptor.getErrorLogs()).isNotEmpty(),
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
